package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import io.gridgo.socket.Socket;
import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;

public abstract class AbstractSocket implements Socket, Loggable {

	private final AtomicBoolean startFlag = new AtomicBoolean(false);
	private volatile boolean started = false;

	@Getter
	private volatile Endpoint endpoint;

	@Override
	public final boolean isAlive() {
		ThreadUtils.busySpin(10, () -> {
			return this.startFlag.get() ^ this.started;
		});
		return this.started;
	}

	@Override
	public void close() {
		if (this.isAlive()) {
			if (this.startFlag.compareAndSet(true, false)) {
				try {
					this.doClose();
				} finally {
					this.started = false;
				}
			}
		}
	}

	@Override
	public final void connect(String address) {
		if (!this.isAlive()) {
			if (this.startFlag.compareAndSet(false, true)) {
				try {
					Endpoint endpoint = EndpointParser.parse(address);
					this.doConnect(endpoint);
					this.endpoint = endpoint;
					this.started = true;
				} catch (Exception ex) {
					this.startFlag.set(false);
				}
			}
		} else {
			throw new IllegalStateException("Socket already started");
		}
	}

	@Override
	public void bind(String address) {
		if (!this.isAlive()) {
			if (this.startFlag.compareAndSet(false, true)) {
				try {
					Endpoint endpoint = EndpointParser.parse(address);
					doBind(endpoint);
					this.endpoint = endpoint;
					this.started = true;
				} catch (Exception ex) {
					this.startFlag.set(false);
				}
			}
		} else {
			throw new IllegalStateException("Socket already started");
		}
	}

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

	protected abstract void doClose();

	protected abstract void doConnect(Endpoint endpoint);

	protected abstract void doBind(Endpoint endpoint);

	protected abstract int doSend(ByteBuffer buffer, boolean block);

	protected abstract int doReveive(ByteBuffer buffer, boolean block);
}
