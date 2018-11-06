package io.gridgo.socket.impl;

import java.nio.ByteBuffer;

import io.gridgo.bean.BArray;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketProducer;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;

public class DefaultSocketProducer extends SingleThreadSendingProducer implements SocketProducer, Loggable {

	private final Socket socket;

	private final ByteBuffer buffer;

	@Getter
	private long totalSentBytes;

	@Getter
	private long totalSentMessages;

	public DefaultSocketProducer(Socket socket, int bufferSize, int ringBufferSize) {
		super(ringBufferSize);
		this.socket = socket;
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
	}

	public DefaultSocketProducer(Socket socket, int bufferSize) {
		this(socket, bufferSize, 1024);
	}

	public DefaultSocketProducer(Socket socket) {
		this(socket, 128 * 1024);
	}

	@Override
	protected void executeSendOnSingleThread(Message message) throws Exception {
		buffer.clear();
		Payload payload = message.getPayload();
		BArray.newFromSequence(payload.getId(), payload.getHeaders(), payload.getBody()).writeBytes(buffer);
		buffer.flip();
		int sentBytes = this.socket.send(buffer);
		if (sentBytes == -1) {
			throw new SendMessageException();
		}
		totalSentBytes += sentBytes;
		totalSentMessages++;
	}

}
