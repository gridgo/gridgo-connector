package io.gridgo.socket.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import io.gridgo.socket.Bindable;
import io.gridgo.socket.Configurable;
import io.gridgo.socket.Connectable;
import io.gridgo.socket.HasEndpoint;
import io.gridgo.socket.Socket;
import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.helper.Loggable;
import lombok.Getter;

public abstract class AbstractSocket<Payload>
		implements Socket<Payload>, Connectable, Bindable, HasEndpoint, Configurable, Loggable {

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
	public final void close() {
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

	protected abstract void doClose();

	protected abstract void doConnect(Endpoint endpoint);

	protected abstract void doBind(Endpoint endpoint);
}
