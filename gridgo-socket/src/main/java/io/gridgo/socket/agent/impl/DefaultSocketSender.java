package io.gridgo.socket.agent.impl;

import io.gridgo.socket.SocketPayload;
import io.gridgo.socket.agent.SocketSender;
import lombok.Getter;

public class DefaultSocketSender extends AbstractSocketAgent implements SocketSender {

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

	}

	@Override
	public int send(SocketPayload payload, boolean block) {
		if (this.isStarted()) {
			int sent = this.getSocket().send(payload.getBuffer(), block);
			this.totalSentBytes += sent;
			this.totalSentMsg++;
			return sent;
		} else {
			throw new IllegalStateException("Cannot send message while not being started");
		}
	}
}
