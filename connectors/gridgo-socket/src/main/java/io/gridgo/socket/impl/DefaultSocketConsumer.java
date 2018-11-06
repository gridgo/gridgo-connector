package io.gridgo.socket.impl;

import java.nio.ByteBuffer;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConsumer;
import io.gridgo.utils.ThreadUtils;
import lombok.Getter;

public class DefaultSocketConsumer extends AbstractConsumer implements SocketConsumer {

	@Getter
	private long totalRecvBytes;

	@Getter
	private long totalRecvMessages;

	private Thread poller;

	private final int bufferSize;
	private final Socket socket;

	public DefaultSocketConsumer(Socket socket, int bufferSize) {
		this.bufferSize = bufferSize;
		this.socket = socket;
	}

	public DefaultSocketConsumer(Socket socket) {
		this(socket, 1024);
	}

	@Override
	protected final void onStop() {
		if (this.poller != null && !this.poller.isInterrupted()) {
			this.poller.interrupt();
		}
	}

	private void poll() {
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
	}

	@Override
	protected void onStart() {
		if (!this.socket.isAlive()) {
			throw new IllegalStateException("Cannot start receiver while socket is not alive");
		}

		if (this.poller != null) {
			throw new IllegalStateException("Poller cannot exist on start");
		}

		this.poller = new Thread(this::poll);
		this.poller.start();

		ThreadUtils.sleep(100);

		this.totalRecvBytes = 0;
		this.totalRecvMessages = 0;
	}
}
