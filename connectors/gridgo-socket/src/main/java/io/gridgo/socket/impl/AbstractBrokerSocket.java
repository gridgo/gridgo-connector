package io.gridgo.socket.impl;

import java.nio.ByteBuffer;

import io.gridgo.socket.BrokerlessSocket;

public abstract class AbstractBrokerSocket extends AbstractSocket<ByteBuffer> implements BrokerlessSocket {

	@Override
	public final int send(ByteBuffer buffer, boolean block) {
		if (this.isAlive()) {
			return doSend(buffer, block);
		} else {
			throw new IllegalStateException("Socket isn't alive");
		}
	}

	@Override
	public final int receive(ByteBuffer buffer, boolean block) {
		if (this.isAlive()) {
			return doReveive(buffer, block);
		} else {
			throw new IllegalStateException("Socket isn't alive");
		}
	}

	protected abstract int doSend(ByteBuffer buffer, boolean block);

	protected abstract int doReveive(ByteBuffer buffer, boolean block);
}
