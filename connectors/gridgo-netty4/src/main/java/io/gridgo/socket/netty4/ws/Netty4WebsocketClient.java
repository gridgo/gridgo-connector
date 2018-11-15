package io.gridgo.socket.netty4.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketClient;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
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

	@Getter
	private Netty4WebsocketFrameType frameType = Netty4WebsocketFrameType.TEXT;

	protected URI getWsUri(HostAndPort host) {
		int port = host.getPortOrDefault(80);
		String wsUri = "ws://" + host.getHostOrDefault("localhost") + (port == 80 ? "" : (":" + port))
				+ (getPath().startsWith("/") ? "" : "/") + this.getPath();
		try {
			return new URI(wsUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid host info" + host);
		}
	}

	private WebSocketClientHandshaker handshaker;

	private ChannelPromise handshakeFuture;

	@Override
	protected void onBeforeConnect(HostAndPort host) {
		URI wsUri = getWsUri(host);
		this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(wsUri, WebSocketVersion.V13, null, false,
				EmptyHttpHeaders.INSTANCE, 1280000);

		String configFrameType = this.getConfigs().getString("frameType", "text");
		this.frameType = Netty4WebsocketFrameType.fromNameOrDefault(configFrameType, Netty4WebsocketFrameType.TEXT);
	}

	@Override
	protected void onAfterConnect() {
		try {
			this.handshakeFuture.sync();
			if (this.getChannelOpenCallback() != null) {
				this.getChannelOpenCallback().run();
			}
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
	protected void onHandlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}

	@Override
	protected void onClose() throws IOException {
		try {
			this.getChannel().writeAndFlush(new CloseWebSocketFrame()).sync();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interupted while trying to write CloseWebSocketFrame", e);
		}
		super.onClose();
	}

	@Override
	protected void onChannelActive(ChannelHandlerContext ctx) throws Exception {
		handshaker.handshake(ctx.channel());
	}

	@Override
	protected void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = ctx.channel();

		if (!handshaker.isHandshakeComplete()) {
			// web socket client connected
			handshaker.finishHandshake(channel, (FullHttpResponse) msg);
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
