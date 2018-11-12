package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4Socket;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.helper.Loggable;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4Socket extends ChannelInboundHandlerAdapter implements Netty4Socket, Loggable {

	@Getter(AccessLevel.PROTECTED)
	private final BObject configs = BObject.newDefault();

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Netty4Transport transport = null;

	private final AtomicBoolean startFlag = new AtomicBoolean(false);
	private boolean running = false;

	@Override
	public final boolean isStarted() {
		ThreadUtils.busySpin(10, () -> {
			return startFlag.get() ^ running;
		});
		return this.running;
	}

	protected final boolean tryStart(Runnable starter) {
		if (!this.isStarted() && this.startFlag.compareAndSet(false, true)) {
			try {
				starter.run();
			} catch (Exception e) {
				this.startFlag.set(false);
				throw e;
			}
			this.running = true;
			return true;
		}
		return false;
	}

	@Override
	public final void close() throws IOException {
		if (this.isStarted() && this.startFlag.compareAndSet(true, false)) {
			this.onClose();
			this.running = false;
		}
	}

	protected void onClose() throws IOException {

	}

	@Override
	public void applyConfig(@NonNull String name, @NonNull Object value) {
		if (this.isStarted()) {
			throw new IllegalStateException("Cannot apply config while this socket already stated");
		}
		this.configs.putAny(name, value);
	}

	protected abstract BElement parseReceivedData(Object msg) throws Exception;
}
