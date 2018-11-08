package io.gridgo.socket.impl;

import java.nio.ByteBuffer;

import io.gridgo.bean.BArray;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.SocketProducer;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;

public class DefaultSocketProducer extends SingleThreadSendingProducer implements SocketProducer, Loggable {

	private final ByteBuffer buffer;

	@Getter
	private long totalSentBytes;

	@Getter
	private long totalSentMessages;

	private final SocketFactory factory;
	private final SocketOptions options;
	private final String address;

	private Socket socket;

	public DefaultSocketProducer(SocketFactory factory, SocketOptions options, String address, int bufferSize,
			int ringBufferSize) {
		super(ringBufferSize);
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.options = options;
		this.factory = factory;
		this.address = address;
	}

	public DefaultSocketProducer(SocketFactory factory, SocketOptions options, String address, int bufferSize) {
		this(factory, options, address, bufferSize, 1024);
	}

	public DefaultSocketProducer(SocketFactory factory, SocketOptions options, String address) {
		this(factory, options, address, 128 * 1024);
	}

	@Override
	protected void onStart() {
		this.socket = this.factory.createSocket(options);
		switch (options.getType().trim().toLowerCase()) {
		case "push":
			socket.connect(address);
			break;
		case "pub":
			socket.bind(address);
			break;
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.socket.close();
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
