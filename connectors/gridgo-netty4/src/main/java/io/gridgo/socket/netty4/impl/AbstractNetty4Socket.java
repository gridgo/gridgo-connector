package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4Socket;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.helper.Loggable;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4Socket implements Netty4Socket, Loggable {

	@Getter(AccessLevel.PROTECTED)
	private final BObject configs = BObject.newDefault();

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Netty4Transport transport = null;

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private HostAndPort host;

	private final AtomicBoolean startFlag = new AtomicBoolean(false);

	private boolean running = false;

	@Setter
	private Consumer<Throwable> failureHandler;

	protected ChannelInboundHandler newChannelHandlerDelegater() {
		return new ChannelInboundHandlerAdapter() {

			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				AbstractNetty4Socket.this.onChannelActive(ctx);
				ctx.fireChannelActive();
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				AbstractNetty4Socket.this.onChannelInactive(ctx);
				ctx.fireChannelInactive();
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				AbstractNetty4Socket.this.onChannelRead(ctx, msg);
				ctx.fireChannelRead(msg);
			}

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				AbstractNetty4Socket.this.onHandlerAdded(ctx);
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				AbstractNetty4Socket.this.onException(ctx, cause);
			}
		};
	}

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
	public void stop() {
		try {
			this.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while close netty4 socket", e);
		}
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
	public final void applyConfig(@NonNull String name, @NonNull Object value) {
		if (this.isStarted()) {
			throw new IllegalStateException("Cannot apply config while this socket already stated");
		}
		this.configs.putAny(name, value);
		this.onApplyConfig(name);
	}

	protected void onApplyConfig(String name) {
		// do nothing
	}

	protected abstract BElement handleIncomingMessage(long channelId, Object msg) throws Exception;

	protected abstract void onChannelActive(ChannelHandlerContext ctx) throws Exception;

	protected abstract void onChannelInactive(ChannelHandlerContext ctx) throws Exception;

	protected abstract void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

	protected void onHandlerAdded(ChannelHandlerContext ctx) {
		// do nothing...
	}

	protected void onException(ChannelHandlerContext ctx, Throwable cause) {
		if (this.failureHandler != null) {
			this.failureHandler.accept(cause);
		} else {
			getLogger().error("Error while handling socket msg", cause);
		}
	}

}