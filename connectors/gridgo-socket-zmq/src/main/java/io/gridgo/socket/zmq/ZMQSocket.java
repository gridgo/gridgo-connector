package io.gridgo.socket.zmq;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

import org.zeromq.ZMQ;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import io.gridgo.utils.ObjectUtils;
import io.gridgo.utils.ObjectUtils.Setter;
import io.gridgo.utils.helper.Assert;
import lombok.NonNull;

final class ZMQSocket extends AbstractSocket {

    private static final Map<String, Setter> ZMQ_SOCKET_SETTERS = initSetters();

    private static Map<String, Setter> initSetters() {
        return ObjectUtils.findAllClassSetters(ZMQ.Socket.class).entrySet().stream().collect(
                Collectors.toMap((Map.Entry<String, Setter> entry) -> entry.getKey().toLowerCase(), (Map.Entry<String, Setter> entry) -> entry.getValue()));
    }

    private final ZMQ.Socket socket;

    ZMQSocket(ZMQ.Socket socket) {
        this.socket = Assert.notNull(socket, "zmq.socket");
    }

    @Override
    public void applyConfig(@NonNull String name, Object value) {
        Setter setter = ZMQ_SOCKET_SETTERS.get(name.toLowerCase());
        if (setter != null) {
            setter.applyAsPrimitive(this.socket, value);
        }
    }

    @Override
    protected void doBind(Endpoint endpoint) {
        String resolvedAddress = endpoint.getResolvedAddress();
        this.socket.bind(resolvedAddress);
        // System.out.println("success bind to: " + resolvedAddress);
    }

    @Override
    protected void doClose() {
        this.socket.close();
    }

    @Override
    protected void doConnect(Endpoint endpoint) {
        String resolvedAddress = endpoint.getResolvedAddress();
        this.socket.connect(resolvedAddress);
        // System.out.println("success connect to address: " + resolvedAddress);
    }

    @Override
    protected int doReveive(ByteBuffer buffer, boolean block) {
        if (buffer.isDirect()) {
            return this.socket.recvZeroCopy(buffer, buffer.capacity(), block ? 0 : ZMQ.NOBLOCK);
        }
        return this.socket.recvByteBuffer(buffer, ZMQ.NOBLOCK);
    }

    @Override
    protected int doSend(ByteBuffer buffer, boolean block) {
        int flags = block ? 0 : ZMQ.NOBLOCK;
        if (!buffer.isDirect()) {
            int pos = buffer.position();
            int len = buffer.limit() - pos;
            if (this.socket.send(buffer.array(), pos, len, flags)) {
                return len;
            }
            return -1;
        }
        return this.socket.sendByteBuffer(buffer, flags);
    }

    @Override
    public int doSubscribe(@NonNull String topic) {
        this.socket.subscribe(topic.getBytes());
        return 0;
    }
}
