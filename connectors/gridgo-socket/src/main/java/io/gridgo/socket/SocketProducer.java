package io.gridgo.socket;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.impl.DefaultSocketProducer;

public interface SocketProducer extends Producer {

	long getTotalSentBytes();

	long getTotalSentMessages();

	@Override
	default Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException();
	}

	static SocketProducer newDefault(SocketFactory factory, SocketOptions options, String address) {
		return new DefaultSocketProducer(factory, options, address);
	}

	static SocketProducer newDefault(SocketFactory factory, SocketOptions options, String address, int bufferSize) {
		return new DefaultSocketProducer(factory, options, address, bufferSize);
	}

	static SocketProducer newDefault(SocketFactory factory, SocketOptions options, String address, int bufferSize,
			int ringBufferSize) {
		return new DefaultSocketProducer(factory, options, address, bufferSize, ringBufferSize);
	}
}
