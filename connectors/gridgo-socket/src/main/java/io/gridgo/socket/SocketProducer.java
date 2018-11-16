package io.gridgo.socket;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.impl.DefaultSocketProducer;

public interface SocketProducer extends Producer {

	long getTotalSentBytes();

	long getTotalSentMessages();

	@Override
	default Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException();
	}

	default boolean isCallSupported() {
		return false;
	}

	static SocketProducer newDefault(ConnectorContext context, SocketFactory factory, SocketOptions options,
			String address, int bufferSize, int ringBufferSize, boolean batchingEnabled, int maxBatchSize) {
		return new DefaultSocketProducer(context, factory, options, address, bufferSize, ringBufferSize,
				batchingEnabled, maxBatchSize);
	}
}
