package io.gridgo.socket.agent.impl;

import java.nio.ByteBuffer;

import io.gridgo.socket.agent.BrockerlessSender;
import lombok.Getter;

public class DefaultSocketSender extends AbstractSocketAgent implements BrockerlessSender {

	@Getter
	private long totalSentBytes = 0;

	@Getter
	private long totalSentMsg = 0;

	@Override
	protected void onStart() {
		if (!this.getSocket().isAlive()) {
			throw new IllegalStateException("Socket must be alive when sender start");
		}
	}

	@Override
	protected void onStop() {
		this.getSocket().close();
	}

	@Override
	public int send(ByteBuffer payload, boolean block) {
		if (this.isStarted()) {
			int sent = this.getSocket().send(payload, block);
			this.totalSentBytes += sent;
			this.totalSentMsg++;
			return sent;
		} else {
			throw new IllegalStateException("Cannot send message while not being started");
		}
	}
}
