package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.Collection;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.SocketConsumer;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.SocketProducer;
import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;
import io.gridgo.utils.helper.Loggable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class DefaultSocketProducer extends SingleThreadSendingProducer implements SocketProducer, Loggable {

    private static final byte ZERO_BYTE = (byte) 0;

    private final ByteBuffer buffer;

    @Getter
    private long totalSentBytes;

    @Getter
    private long totalSentMessages;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Receiver receiver;

    private final SocketFactory factory;

    private final SocketOptions options;

    private final String address;

    private Socket socket;

    public DefaultSocketProducer(//
            ConnectorContext context, //
            SocketFactory factory, //
            SocketOptions options, //
            String address, //
            int bufferSize, //
            int ringBufferSize, //
            boolean batchingEnabled, //
            int maxBatchingSize) {

        super(context, ringBufferSize, (runnable) -> {
            return new Thread(runnable);
        }, batchingEnabled, maxBatchingSize);

        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.options = options;
        this.factory = factory;
        this.address = address;
    }

    @Override
    protected Message accumulateBatch(@NonNull Collection<Message> messages) {
        if (this.isBatchingEnabled()) {
            return SocketUtils.accumulateBatch(messages);
        }
        throw new IllegalStateException("Batching is disabled");
    }

    @Override
    protected void executeSendOnSingleThread(Message message) throws Exception {
        buffer.clear();
        if (options.getType().equalsIgnoreCase("pub")) {
            message.getRoutingId() //
                   .map(BValue::getRaw) //
                   .ifPresent(buffer::put);
            buffer.put(ZERO_BYTE);
        }

        Payload payload = message.getPayload();
        if (payload != null) {
            payload.toBArray().writeBytes(buffer);

            buffer.flip();
            int sentBytes = this.socket.send(buffer);
            if (sentBytes == -1) {
                throw new SendMessageException();
            }
            totalSentBytes += sentBytes;
            totalSentMessages++;
        }
    }

    @Override
    protected String generateName() {
        return "producer." + this.getUniqueIdentifier();
    }

    private String getUniqueIdentifier() {
        return new StringBuilder() //
                                  .append(this.factory.getType()) //
                                  .append(".") //
                                  .append(this.options.getType()) //
                                  .append(".") //
                                  .append(this.address) //
                                  .toString();
    }

    @Override
    public boolean isCallSupported() {
        return false;
    }

    @Override
    protected void onStart() {
        this.socket = this.factory.createSocket(options);
        String type = options.getType().trim().toLowerCase();
        switch (type) {
        case "push":
            socket.connect(address);
            break;
        case "pub":
            Endpoint endpoint = EndpointParser.parse(address);
            if (SocketConnector.MULTICAST_TRANSPORTS.contains(endpoint.getProtocol())) {
                socket.connect(address);
            } else {
                socket.bind(address);
            }
            break;
        case "pair":
            socket.connect(address);
            int bufferSize = Integer.parseInt((String) options.getConfig().getOrDefault("bufferSize", "" + SocketConnector.DEFAULT_BUFFER_SIZE));
            if (!options.getConfig().containsKey("receiveTimeout")) {
                socket.applyConfig("receiveTimeout", SocketConsumer.DEFAULT_RECV_TIMEOUT);
            }
            this.setReceiver(new DefaultSocketReceiver(getContext(), this.socket, bufferSize, getUniqueIdentifier()));
            break;
        default:
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.socket.close();
    }
}
