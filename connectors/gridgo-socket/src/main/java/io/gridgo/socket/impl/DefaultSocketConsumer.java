package io.gridgo.socket.impl;

import java.nio.ByteBuffer;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConsumer;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.utils.ThreadUtils;
import lombok.Getter;

public class DefaultSocketConsumer extends AbstractConsumer implements SocketConsumer {

	@Getter
	private long totalRecvBytes;

	@Getter
	private long totalRecvMessages;

	private Thread poller;

	private final int bufferSize;

	private final SocketFactory factory;
	private final SocketOptions options;
	private final String address;

	public DefaultSocketConsumer(SocketFactory factory, SocketOptions options, String address, int bufferSize) {
		this.factory = factory;
		this.options = options;
		this.address = address;
		this.bufferSize = bufferSize;
	}

	public DefaultSocketConsumer(SocketFactory factory, SocketOptions options, String address) {
		this(factory, options, address, 1024);
	}

	@Override
	protected final void onStop() {
		if (this.poller != null && !this.poller.isInterrupted()) {
			this.poller.interrupt();
		}
	}

	private void poll() {
		Socket socket = this.factory.createSocket(options);
		switch (options.getType().toLowerCase()) {
		case "pull":
			socket.bind(address);
			break;
		case "sub":
			socket.connect(address);
			break;
		}

		Thread.currentThread().setName("[POLLER] " + socket.getEndpoint().getAddress());
		final ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferSize);
		while (!Thread.currentThread().isInterrupted()) {
			buffer.clear();
			int rc = socket.receive(buffer);

			if (rc < 0) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				// otherwise, socket timeout occurred, continue event loop
			} else {
				totalRecvBytes += rc;
				totalRecvMessages++;
				buffer.flip();
				BElement data = BElement.fromRaw(buffer);
				Payload payload = Payload.newDefault(data);
				Message message = Message.newDefault(payload);
				this.publish(message, null);
			}
		}
		socket.close();
		this.poller = null;
	}

	@Override
	protected void onStart() {
		this.poller = new Thread(this::poll);
		this.poller.start();

		ThreadUtils.sleep(100);

		this.totalRecvBytes = 0;
		this.totalRecvMessages = 0;
	}
}
