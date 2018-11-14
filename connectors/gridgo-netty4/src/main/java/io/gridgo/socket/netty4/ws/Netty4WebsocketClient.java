package io.gridgo.socket.netty4.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.CharsetUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Netty4WebsocketClient extends AbstractNetty4SocketClient implements Netty4Websocket {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private String path;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private String proxy;

	private Netty4WebsocketFrameType frameType = Netty4WebsocketFrameType.TEXT;

	protected URI getWsUri() {
		int port = this.getHost().getPortOrDefault(80);
		String wsUri = "ws://" + this.getHost().getHostOrDefault("localhost") + (port == 80 ? "" : (":" + port))
				+ (getPath().startsWith("/") ? "" : "/") + this.getPath();
		try {
			return new URI(wsUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid host info" + this.getHost());
		}
	}

	private WebSocketClientHandshaker handshaker;

	private ChannelPromise handshakeFuture;

	protected WebSocketClientHandshaker getHandshaker() {
		if (this.handshaker == null) {
			synchronized (this) {
				if (this.handshaker == null) {
					this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(getWsUri(), WebSocketVersion.V13,
							null, false, EmptyHttpHeaders.INSTANCE, 1280000);
				}
			}
		}
		return this.handshaker;
	}

	public Netty4WebsocketFrameType getFrameType() {
		if (frameType == null) {
			synchronized (this) {
				if (frameType == null) {
					String configFrameType = this.getConfigs().getString("frameType", "text");
					this.frameType = Netty4WebsocketFrameType.fromName(configFrameType);

					if (this.frameType == null) {
						this.frameType = Netty4WebsocketFrameType.TEXT;
					}
				}
			}
		}
		return frameType;
	}

	@Override
	protected void onConnectionEstablished() {
		try {
			this.handshakeFuture.sync();
		} catch (InterruptedException e) {
			throw new RuntimeException("Waiting for handshake error", e);
		}
	}

	@Override
	protected void onInitChannel(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("http-codec", new HttpClientCodec());
		pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
	}

	@Override
	protected Bootstrap createBootstrap() {
		return new Bootstrap().channel(NioSocketChannel.class);
	}

	@Override
	protected void onHandlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}

	@Override
	protected void onClose() throws IOException {
		this.getChannel().writeAndFlush(new CloseWebSocketFrame());
		super.onClose();
	}

	@Override
	protected void onChannelActive(ChannelHandlerContext ctx) throws Exception {
		getHandshaker().handshake(ctx.channel());
	}

	@Override
	protected void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = ctx.channel();

		if (!getHandshaker().isHandshakeComplete()) {
			// web socket client connected
			getHandshaker().finishHandshake(channel, (FullHttpResponse) msg);
			handshakeFuture.setSuccess();
			return;
		}

		if (msg instanceof CloseWebSocketFrame) {
			this.close();
		} else if (msg instanceof FullHttpResponse) {
			final FullHttpResponse response = (FullHttpResponse) msg;
			throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		} else if (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame) {
			super.onChannelRead(ctx, msg);
		}
	}

	@Override
	protected BElement handleIncomingMessage(Object msg) throws Exception {
		return Netty4WebsocketUtils.parseWebsocketFrame((WebSocketFrame) msg);
	}

	@Override
	public ChannelFuture send(BElement data) {
		return Netty4WebsocketUtils.send(getChannel(), data, getFrameType());
	}
}