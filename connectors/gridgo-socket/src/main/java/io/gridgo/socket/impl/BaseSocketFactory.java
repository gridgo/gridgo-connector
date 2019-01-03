package io.gridgo.socket.impl;

import io.gridgo.socket.Configurable;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.utils.helper.Assert;

public abstract class BaseSocketFactory extends AbstractSocketFactory {

    protected Socket createCustomSocket(String type) {
        return null;
    }

    protected Socket createPairSocket() {
        return null;
    }

    protected Socket createPubSocket() {
        return null;
    }

    protected Socket createPullSocket() {
        return null;
    }

    protected Socket createPushSocket() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends Socket> T createSocket(SocketOptions options) {
        Assert.notNull(options, "socket options");
        Assert.notNull(options.getType(), "socket type");

        Socket socket = this.createCustomSocket(options.getType());

        if (socket == null) {
            switch (options.getType().toLowerCase()) {
            case "push":
                socket = this.createPushSocket();
                break;
            case "pull":
                socket = this.createPullSocket();
                break;
            case "pub":
                socket = this.createPubSocket();
                break;
            case "sub":
                socket = this.createSubSocket();
                break;
            case "pair":
                socket = this.createPairSocket();
                break;
            default:
            }
        }

        if (socket != null) {
            if (socket instanceof Configurable) {
                ((Configurable) socket).applyConfig(options.getConfig());
            }
            return (T) socket;
        }

        throw new IllegalArgumentException("Socket type " + options.getType() + " is not supported");
    }

    protected Socket createSubSocket() {
        return null;
    }

}
