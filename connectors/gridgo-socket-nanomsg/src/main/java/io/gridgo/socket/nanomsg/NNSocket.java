package io.gridgo.socket.nanomsg;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.nanomsg.NanoLibrary;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import io.gridgo.utils.PrimitiveUtils;
import io.gridgo.utils.helper.Assert;
import lombok.AccessLevel;
import lombok.Getter;

public class NNSocket extends AbstractSocket {

    @Getter(AccessLevel.PROTECTED)
    private final int id;

    @Getter(AccessLevel.PROTECTED)
    private final NanoLibrary nanomsg;

    NNSocket(int id, NanoLibrary nanoLibrary) {
        this.id = id;
        this.nanomsg = Assert.notNull(nanoLibrary, "Nanomsg");
    }

    private boolean applyConfig(int option, int value) {
        return nanomsg.nn_setsockopt_int(this.getId(), nanomsg.NN_SOL_SOCKET, option, value) >= 0;
    }

    @Override
    public void applyConfig(String name, Object value) {
        Assert.notNull(name, "Config's name");
        Assert.notNull(value, "Config's value");

        boolean success = false;
        switch (name.toLowerCase()) {
        case "linger":
            success = this.applyConfig(nanomsg.NN_LINGER, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "sendbuffer":
        case "sndbuf":
            success = this.applyConfig(nanomsg.NN_SNDBUF, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "receivebuffer":
        case "recvbuffer":
        case "recvbuf":
        case "rcvbuf":
            success = this.applyConfig(nanomsg.NN_RCVBUF, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "sendtimeout":
        case "sndtimeout":
        case "sendtimeo":
        case "sndtimeo":
            success = this.applyConfig(nanomsg.NN_SNDTIMEO, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "receivetimeout":
        case "recvtimeout":
        case "rcvtimeo":
            success = this.applyConfig(nanomsg.NN_RCVTIMEO, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "reconnectinterval":
        case "reconnect_ivl":
        case "reconnectivl":
            success = this.applyConfig(nanomsg.NN_RECONNECT_IVL, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "reconnectivlmax":
        case "reconnect_ivl_max":
        case "reconnectintervalmax":
            success = this.applyConfig(nanomsg.NN_RECONNECT_IVL_MAX, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        case "sendpriority":
        case "sndpriority":
        case "sendprio":
        case "sndprio":
            success = this.applyConfig(nanomsg.NN_SNDPRIO, PrimitiveUtils.getIntegerValueFrom(value));
            break;
        default:
        }

        if (success) {
            // System.out.println("[NNSocket] applied config " + name + " with value " +
            // value);
        }
    }

    @Override
    protected void doBind(Endpoint endpoint) {
        nanomsg.nn_bind(getId(), endpoint.getResolvedAddress());
    }

    @Override
    protected void doClose() {
        nanomsg.nn_close(getId());
    }

    @Override
    protected void doConnect(Endpoint endpoint) {
        nanomsg.nn_connect(getId(), endpoint.getResolvedAddress());
    }

    @Override
    protected int doReveive(ByteBuffer buffer, boolean block) {
        return nanomsg.nn_recv(id, buffer, block ? 0 : nanomsg.NN_DONTWAIT);
    }

    @Override
    protected int doSend(ByteBuffer buffer, boolean block) {
        int flags = block ? 0 : nanomsg.NN_DONTWAIT;
        if (buffer.isDirect()) {
            return nanomsg.nn_send(getId(), buffer, flags);
        }
        int pos = buffer.position();
        int limit = buffer.limit();
        byte[] bytes = Arrays.copyOfRange(buffer.array(), pos, limit);
        nanomsg.nn_sendbyte(getId(), bytes, flags);
        return bytes.length;
    }

    @Override
    protected int doSubscribe(String topic) {
        return nanomsg.nn_setsockopt_str(this.id, nanomsg.NN_SUB, nanomsg.NN_SUB_SUBSCRIBE, topic);
    }
}
