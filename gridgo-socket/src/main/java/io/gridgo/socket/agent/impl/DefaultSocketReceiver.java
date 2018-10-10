package io.gridgo.socket.agent.impl;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import io.gridgo.socket.agent.SocketReceiver;
import io.gridgo.utils.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class DefaultSocketReceiver extends AbstractSocketAgent implements SocketReceiver {

	@Getter
	private long totalRecvBytes = 0;

	@Getter
	private long totalRecvMsg = 0;

	@Setter
	private int bufferSize;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private BiConsumer<Integer, ByteBuffer> consumer;

	private Thread poller;

	@Override
	protected final void onStop() {
		if (this.poller != null && !this.poller.isInterrupted()) {
			this.poller.interrupt();
			this.onFinally();
		}
	}

	protected void onFinally() {
		// TODO Auto-generated method stub

	}

	private void poll() {
		Thread.currentThread().setName("[POLLER] " + getSocket().getEndpoint().getAddress());
		final ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferSize);
		while (!Thread.currentThread().isInterrupted()) {
			buffer.clear();
			int rc = getSocket().receive(buffer);
			if (rc < 0) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				} else {
					// socket timeout, continue event loop
				}
			} else if (rc > 0) {
				totalRecvBytes += rc;
				totalRecvMsg++;
				buffer.flip();
				if (this.consumer != null) {
					try {
						this.consumer.accept(rc, buffer);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		getSocket().close();
	}

	@Override
	protected void onStart() {
		if (this.poller != null) {
			throw new IllegalStateException("Poller cannot exist on start");
		}

		this.poller = new Thread(this::poll);
		this.poller.start();

		ThreadUtils.sleep(100);

		this.totalRecvBytes = 0;
		this.totalRecvMsg = 0;
		this.onStartSuccess();
	}

	protected void onStartSuccess() {

	}
}
