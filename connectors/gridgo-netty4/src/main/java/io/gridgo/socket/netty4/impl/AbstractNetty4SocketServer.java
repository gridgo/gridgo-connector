package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.utils.support.HostAndPort;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4SocketServer extends AbstractNetty4Socket implements Netty4SocketServer {

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

	private final Map<Long, ChannelHandlerContext> channelContexts = new NonBlockingHashMap<>();

	private final ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {

		@Override
		public void initChannel(SocketChannel socketChannel) throws Exception {
			AbstractNetty4SocketServer.this.initChannel(socketChannel);
		}
	};

	private NioEventLoopGroup bossGroup;

	private NioEventLoopGroup workerGroup;

	@Override
	public void bind(@NonNull final HostAndPort host) {
		tryStart(() -> {
			this.executeBind(host);
		});
	}

	protected abstract ServerBootstrap createBootstrap();

	private void executeBind(HostAndPort host) {
		BObject configs = this.getConfigs();

		bossGroup = new NioEventLoopGroup(configs.getInteger("bootThreads", 1));
		workerGroup = new NioEventLoopGroup(configs.getInteger("workerThreads", 1));

		ServerBootstrap bootstrap = createBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.childHandler(this.channelInitializer);

		// Bind and start to accept incoming connections.
		ChannelFuture channelFuture = bootstrap.bind(host.getResolvedIpOrDefault("127.0.0.1"), host.getPort());

		try {
			if (!channelFuture.await().isSuccess()) {
				throw new RuntimeException("Start " + this.getClass().getName() + " is unsuccessful");
			} else {
				System.out.println("Bind success to " + host.toIpAndPort());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onClose() throws IOException {
		this.bossGroup.shutdownGracefully();
		this.workerGroup.shutdownGracefully();
	}

	private void initChannel(SocketChannel socketChannel) {
		System.out.println("Init channel");
		this.onInitChannel(socketChannel);
		socketChannel.pipeline().addLast(this);
	}

	protected abstract void onInitChannel(SocketChannel socketChannel);

	protected Long getChannelId(ChannelHandlerContext ctx) {
		if (ctx != null) {
			return (Long) ctx.channel().attr(AttributeKey.valueOf("channelId")).get();
		}
		return null;
	}

	@Override
	public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Long id = this.getChannelId(ctx);
		if (id != null) {
			if (this.getReceiveCallback() != null) {
				this.getReceiveCallback().accept(id, parseReceivedData(msg));
			}
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public final void channelActive(ChannelHandlerContext ctx) throws Exception {
		long id = ID_SEED.getAndIncrement();
		this.channelContexts.put(id, ctx);
		ctx.channel().attr(AttributeKey.newInstance("channelId")).set(id);
		if (this.getChannelOpenCallback() != null) {
			this.getChannelOpenCallback().accept(id);
		}
		ctx.fireChannelActive();
	}

	@Override
	public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Long id = getChannelId(ctx);
		if (id != null && this.channelContexts.containsKey(id)) {
			if (ctx == this.channelContexts.get(id)) {
				ChannelHandlerContext context = this.channelContexts.remove(id);
				if (this.getChannelCloseCallback() != null) {
					this.getChannelCloseCallback().accept(id);
				}
				context.fireChannelInactive();
			} else {
				throw new IllegalStateException(
						"Something were wrong, the current inactive channel has registered with other channel context");
			}
		} else {
			getLogger().warn("The current inactive channel hasn't been registered");
		}
	}

	@Override
	public final ChannelFuture send(long routingId, BElement data) {
		ChannelHandlerContext ctx = this.channelContexts.get(routingId);
		if (ctx != null) {
			if (data == null) {
				ctx.close();
			} else {
				return ctx.writeAndFlush(data);
			}
		}
		return null;
	}
}
