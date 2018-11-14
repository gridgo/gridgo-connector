package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4SocketOptionsUtils;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.utils.support.HostAndPort;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4SocketServer extends AbstractNetty4Socket implements Netty4SocketServer {

	private static final AttributeKey<Object> CHANNEL_ID = AttributeKey.newInstance("channelId");

	private static final AtomicLong ID_SEED = new AtomicLong(0);

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private BiConsumer<Long, BElement> receiveCallback;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Consumer<Long> channelOpenCallback;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Consumer<Long> channelCloseCallback;

	private final Map<Long, Channel> channels = new NonBlockingHashMap<>();

	private final ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {

		@Override
		public void initChannel(SocketChannel socketChannel) throws Exception {
			AbstractNetty4SocketServer.this.initChannel(socketChannel);
		}
	};

	private NioEventLoopGroup bossGroup;

	private NioEventLoopGroup workerGroup;

	private ChannelFuture bindFuture;

	private ServerBootstrap bootstrap;

	@Override
	public void bind(@NonNull final HostAndPort host) {
		tryStart(() -> {
			this.executeBind(host);
		});
	}

	protected ServerBootstrap createBootstrap() {
		return new ServerBootstrap().channel(NioServerSocketChannel.class);
	}

	private void executeBind(HostAndPort host) {
		BObject configs = this.getConfigs();

		bossGroup = new NioEventLoopGroup(configs.getInteger("bootThreads", 1));
		workerGroup = new NioEventLoopGroup(configs.getInteger("workerThreads", 1));

		bootstrap = createBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.childHandler(this.channelInitializer);

		Netty4SocketOptionsUtils.applyOptions(getConfigs(), bootstrap);

		// Bind and start to accept incoming connections.
		bindFuture = bootstrap.bind(host.getResolvedIpOrDefault("127.0.0.1"), host.getPort());

		try {
			if (!bindFuture.await().isSuccess()) {
				throw new RuntimeException("Start " + this.getClass().getName() + " is unsuccessful",
						bindFuture.cause());
			} else {
				getLogger().info("Bind success to %s", host.toIpAndPort());
				this.setHost(host);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onApplyConfig(String name) {
		Netty4SocketOptionsUtils.applyOption(name, getConfigs(), bootstrap);
	}

	@Override
	protected void onClose() throws IOException {
		for (Channel channel : this.channels.values()) {
			closeChannel(channel);
		}

		this.channels.clear();

		this.bindFuture.channel().close();

		this.bossGroup.shutdownGracefully();
		this.workerGroup.shutdownGracefully();

		this.bindFuture = null;
		this.bossGroup = null;
		this.workerGroup = null;
	}

	protected void closeChannel(Channel channel) {
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			// continue
		}
	}

	private void initChannel(SocketChannel socketChannel) {
		this.onInitChannel(socketChannel);
		socketChannel.pipeline().addLast(this.newChannelHandlerDelegater());
	}

	protected abstract void onInitChannel(SocketChannel socketChannel);

	protected Long getChannelId(Channel channel) {
		if (channel != null) {
			return (Long) channel.attr(CHANNEL_ID).get();
		}
		return null;
	}

	protected Channel getChannel(long id) {
		return this.channels.get(id);
	}

	@Override
	protected final void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final Channel channel = ctx.channel();
		Long id = this.getChannelId(channel);
		if (id != null) {
			if (this.getReceiveCallback() != null) {
				BElement incomingMessage = handleIncomingMessage(id, msg);
				if (incomingMessage != null) {
					this.getReceiveCallback().accept(id, incomingMessage);
				}
			}
		}
	}

	@Override
	protected final void onChannelActive(final ChannelHandlerContext ctx) throws Exception {
		final Channel channel = ctx.channel();
		final long id = ID_SEED.getAndIncrement();

		channel.attr(CHANNEL_ID).set(id);
		this.channels.put(id, channel);

		if (this.getChannelOpenCallback() != null) {
			this.getChannelOpenCallback().accept(id);
		}
	}

	@Override
	protected final void onChannelInactive(ChannelHandlerContext ctx) throws Exception {
		final Channel channel = ctx.channel();
		Long id = getChannelId(channel);
		if (id != null && this.channels.containsKey(id)) {
			if (channel == this.channels.get(id)) {
				this.channels.remove(id);
				if (this.getChannelCloseCallback() != null) {
					this.getChannelCloseCallback().accept(id);
				}
			} else {
				throw new IllegalStateException(
						"Something were wrong, the current inactive channel has registered with other channel context");
			}
		} else {
			getLogger().warn("The current inactive channel hasn't been registered");
		}
	}
}
