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

	static SocketProducer newDefault(Socket socket) {
		return new DefaultSocketProducer(socket);
	}

	static SocketProducer newDefault(Socket socket, int bufferSize) {
		return new DefaultSocketProducer(socket, bufferSize);
	}

	static SocketProducer newDefault(Socket socket, int bufferSize, int ringBufferSize) {
		return new DefaultSocketProducer(socket, bufferSize, ringBufferSize);
	}
}
