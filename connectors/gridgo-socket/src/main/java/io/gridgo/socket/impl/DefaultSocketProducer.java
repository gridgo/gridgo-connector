package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.Collection;

import io.gridgo.bean.BArray;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConstants;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.SocketProducer;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;
import lombok.NonNull;

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

	public DefaultSocketProducer(//
			ConnectorContext context, //
			SocketFactory factory, //
			SocketOptions options, //
			String address, //
			int bufferSize, //
			int ringBufferSize, //
			boolean batchingEnabled, int maxBatchingSize) {

		super(context, ringBufferSize, (runnable) -> {
			return new Thread(runnable);
		}, batchingEnabled, maxBatchingSize);

		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.options = options;
		this.factory = factory;
		this.address = address;
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
	protected Message accumulateBatch(@NonNull Collection<Message> messages) {
		if (messages.size() == 1) {
			return messages.iterator().next();
		}

		if (this.isBatchingEnabled()) {
			BArray body = BArray.newDefault();
			for (Message mess : messages) {
				Payload payload = mess.getPayload();
				body.add(BArray.newFromSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody()));
			}
			Payload payload = Payload.newDefault(body)//
					.addHeader(SocketConstants.IS_BATCH, true) //
					.addHeader(SocketConstants.BATCH_SIZE, messages.size());

			return Message.newDefault(payload);
		}
		throw new IllegalStateException("Batching is disabled");
	}

	@Override
	protected void executeSendOnSingleThread(Message message) throws Exception {
		buffer.clear();
		Payload payload = message.getPayload();
		BArray.newFromSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody())
				.writeBytes(buffer);
		buffer.flip();
		int sentBytes = this.socket.send(buffer);
		if (sentBytes == -1) {
			throw new SendMessageException();
		}
		totalSentBytes += sentBytes;
		totalSentMessages++;
	}
}
