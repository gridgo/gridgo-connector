package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.Netty4SocketClient;
import io.gridgo.socket.netty4.Netty4SocketOptionsUtils;
import io.gridgo.utils.support.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4SocketClient extends AbstractNetty4Socket implements Netty4SocketClient {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Consumer<BElement> receiveCallback;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Runnable channelOpenCallback;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Runnable channelCloseCallback;

	@Getter(AccessLevel.PROTECTED)
	private Channel channel;

	private ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			AbstractNetty4SocketClient.this.initChannel(ch);
		}
	};

	private Bootstrap bootstrap;

	@Override
	public void connect(@NonNull final HostAndPort host) {
		tryStart(() -> {
			this.executeConnect(host);
		});
	}

	private void initChannel(SocketChannel channel) {
		this.onInitChannel(channel);
		channel.pipeline().addLast("handler", this.newChannelHandlerDelegater());
	}

	protected abstract void onInitChannel(SocketChannel ch);

	protected Bootstrap createBootstrap() {
		return new Bootstrap().channel(NioSocketChannel.class);
	}

	private void executeConnect(HostAndPort host) {
		bootstrap = createBootstrap();
		bootstrap.group(createLoopGroup());
		bootstrap.handler(this.channelInitializer);

		Netty4SocketOptionsUtils.applyOptions(getConfigs(), bootstrap);

		try {
			ChannelFuture future = bootstrap.connect(host.getHostOrDefault("localhost"), host.getPort());
			if (!future.await().isSuccess()) {
				throw new RuntimeException("Cannot connect to " + host);
			} else {
				this.setHost(host);
				this.channel = future.channel();
				this.onConnectionEstablished();
				getLogger().info("Connect success to %s", host.toIpAndPort());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while connect to " + host, e);
		}
	}

	@Override
	protected void onApplyConfig(String name) {
		Netty4SocketOptionsUtils.applyOption(name, getConfigs(), bootstrap);
	}

	protected void onConnectionEstablished() {
		// do nothing
	}

	protected NioEventLoopGroup createLoopGroup() {
		return new NioEventLoopGroup(this.getConfigs().getInteger("workerThreads", 1));
	}

	@Override
	protected void onChannelActive(ChannelHandlerContext ctx) throws Exception {
		if (this.getChannelOpenCallback() != null) {
			this.getChannelOpenCallback().run();
		}
	}

	@Override
	protected final void onChannelInactive(ChannelHandlerContext ctx) throws Exception {
		if (this.getChannelCloseCallback() != null) {
			this.getChannelCloseCallback().run();
		}
	}

	@Override
	protected void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (this.getReceiveCallback() != null) {
			this.getReceiveCallback().accept(this.handleIncomingMessage(0, msg));
		}
	}

	@Override
	public ChannelFuture send(BElement data) {
		return this.channel.writeAndFlush(data);
	}

	@Override
	protected final BElement handleIncomingMessage(long channelId, Object msg) throws Exception {
		return this.handleIncomingMessage(msg);
	}

	@Override
	protected void onClose() throws IOException {
		try {
			this.getChannel().close().sync();
		} catch (InterruptedException e) {
			// ignore
		}
	}

	protected abstract BElement handleIncomingMessage(Object msg) throws Exception;
}
