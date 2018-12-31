package io.gridgo.socket;

import org.joo.promise4j.Promise;

import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.impl.DefaultSocketProducer;

public interface SocketProducer extends Producer, HasReceiver {

    static SocketProducer of(ConnectorContext context, SocketFactory factory, SocketOptions options, String address, int bufferSize, int ringBufferSize,
            boolean batchingEnabled, int maxBatchSize) {
        return new DefaultSocketProducer(context, factory, options, address, bufferSize, ringBufferSize, batchingEnabled, maxBatchSize);
    }

    @Override
    default Promise<Message, Exception> call(Message request) {
        throw new UnsupportedOperationException();
    }

    long getTotalSentBytes();

    long getTotalSentMessages();

    default boolean isCallSupported() {
        return false;
    }
}
