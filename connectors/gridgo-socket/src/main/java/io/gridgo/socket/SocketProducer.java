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

	static SocketProducer newDefault(Socket socket, String type, String address) {
		return new DefaultSocketProducer(socket, type, address);
	}

	static SocketProducer newDefault(Socket socket, String type, String address, int bufferSize) {
		return new DefaultSocketProducer(socket, type, address, bufferSize);
	}

	static SocketProducer newDefault(Socket socket, String type, String address, int bufferSize, int ringBufferSize) {
		return new DefaultSocketProducer(socket, type, address, bufferSize, ringBufferSize);
	}
}
