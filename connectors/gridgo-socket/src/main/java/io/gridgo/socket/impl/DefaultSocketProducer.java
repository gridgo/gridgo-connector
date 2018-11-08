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

	private String type;

	private String address;

	public DefaultSocketProducer(Socket socket, String type, String address, int bufferSize, int ringBufferSize) {
		super(ringBufferSize);
		this.socket = socket;
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.type = type;
		this.address = address;
	}

	public DefaultSocketProducer(Socket socket, String type, String address, int bufferSize) {
		this(socket, type, address, bufferSize, 1024);
	}

	public DefaultSocketProducer(Socket socket, String type, String address) {
		this(socket, type, address, 128 * 1024);
	}

	public void onStart() {
		switch (type) {
		case "push":
			socket.connect(address);
			break;
		case "pub":
			socket.bind(address);
			break;
		}
	}

	public void onStop() {
		// TODO close socket
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
