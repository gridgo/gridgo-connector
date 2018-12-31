package io.gridgo.socket.zmq;

import org.zeromq.ZMQ;

import io.gridgo.socket.Socket;
import io.gridgo.socket.impl.BaseSocketFactory;
import lombok.AccessLevel;
import lombok.Getter;
import net.jodah.failsafe.internal.util.Assert;

public class ZMQSocketFactory extends BaseSocketFactory {

    @Getter(AccessLevel.PROTECTED)
    private final ZMQ.Context ctx;

    @Getter
    private final String type = "zmq";

    public ZMQSocketFactory() {
        this(1);
    }

    public ZMQSocketFactory(int ioThreads) {
        this.ctx = ZMQ.context(ioThreads);
    }

    protected Socket createCustomSocket(String type) {
        Assert.notNull(type, "Socket type");
        switch (type.toLowerCase()) {
        case "router":
            return createZmqSocket(ZMQ.ROUTER);
        case "dealer":
            return createZmqSocket(ZMQ.DEALER);
        default:
        }
        return null;
    }

    protected Socket createPairSocket() {
        return createZmqSocket(ZMQ.PAIR);
    }

    protected Socket createPubSocket() {
        return createZmqSocket(ZMQ.PUB);
    }

    protected Socket createPullSocket() {
        return createZmqSocket(ZMQ.PULL);
    }

    protected Socket createPushSocket() {
        return createZmqSocket(ZMQ.PUSH);
    }

    protected Socket createRepSocket() {
        return createZmqSocket(ZMQ.REP);
    }

    protected Socket createReqSocket() {
        return createZmqSocket(ZMQ.REQ);
    }

    protected Socket createSubSocket() {
        return createZmqSocket(ZMQ.SUB);
    }

    private Socket createZmqSocket(int type) {
        return new ZMQSocket(ctx.socket(type));
    }
}
