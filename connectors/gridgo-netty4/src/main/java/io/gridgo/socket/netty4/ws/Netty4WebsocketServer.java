package io.gridgo.socket.netty4.ws;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Netty4WebsocketServer extends AbstractNetty4SocketServer implements Netty4Websocket {

	private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_KEY = AttributeKey
			.newInstance("handshaker");

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private String path;

	private WebSocketServerHandshakerFactory wsFactory;

	private Netty4WebsocketFrameType frameType;

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

	protected String getWsUri() {
		String proxy = this.getConfigs().getString("proxy", null);
		String wsUri = proxy == null ? null : proxy;
		if (wsUri == null) {
			wsUri = "ws://" + this.getHost().toHostAndPort() + (getPath().startsWith("/") ? "" : "/") + this.getPath();
		}
		return wsUri;
	}

	protected WebSocketServerHandshakerFactory getWsFactory() {
		if (wsFactory == null) {
			synchronized (this) {
				if (wsFactory == null) {
					wsFactory = new WebSocketServerHandshakerFactory(getWsUri(), null, true);
				}
			}
		}
		return this.wsFactory;
	}

	@Override
	protected BElement handleIncomingMessage(long channelId, Object msg) throws Exception {
		Channel ctx = getChannel(channelId);
		if (ctx != null) {
			try {
				if (msg instanceof FullHttpRequest) {
					handleHttpRequest(ctx, (FullHttpRequest) msg);
				} else if (msg instanceof WebSocketFrame) {
					return handleWebSocketFrame(ctx, (WebSocketFrame) msg);
				}
			} finally {
				if (msg instanceof ReferenceCounted) {
					try {
						((ReferenceCounted) msg).release();
					} catch (Exception e) {
						getLogger().error("Error while retaining websocket message", e);
					}
				}
			}
		}
		return null;
	}

	protected BElement handleWebSocketFrame(Channel channel, WebSocketFrame frame) {
		if (frame == null) {
			return null;
		}

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			WebSocketServerHandshaker handshaker = channel.attr(HANDSHAKER_KEY).get();
			handshaker.close(channel, (CloseWebSocketFrame) frame.retain());
			return null;
		}

		if (frame instanceof PingWebSocketFrame) {
			channel.write(new PongWebSocketFrame(frame.content().retain()));
			return null;
		}

		return Netty4WebsocketUtils.parseWebsocketFrame(frame);
	}

	private static void sendHttpResponse(Channel channel, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = channel.writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@SuppressWarnings("deprecation")
	public void handleHttpRequest(Channel channel, FullHttpRequest req) {
		// Handle a bad request.
		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(channel, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.method() != GET) {
			sendHttpResponse(channel, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		if ("/".equals(req.uri())) {
			ByteBuf content = Unpooled
					.copiedBuffer("Default gridgo-netty4 connector websocket welcome page".getBytes());
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpUtil.setContentLength(res, content.readableBytes());

			sendHttpResponse(channel, req, res);
			return;
		}

		if ("/favicon.ico".equals(req.uri())) {
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			sendHttpResponse(channel, req, res);
			return;
		}

		// Handshake
		final WebSocketServerHandshaker handshaker = getWsFactory().newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
		} else {
			handshaker.handshake(channel, req);
			channel.attr(HANDSHAKER_KEY).set(handshaker);
		}
	}

	@Override
	protected void closeChannel(Channel channel) {
		channel.writeAndFlush(new CloseWebSocketFrame());
		super.closeChannel(channel);
	}

	@Override
	protected void onInitChannel(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
	}

	@Override
	public ChannelFuture send(long routingId, BElement data) {
		Channel channel = this.getChannel(routingId);
		if (data == null) {
			closeChannel(channel);
			return null;
		} else {
			return Netty4WebsocketUtils.send(channel, data, getFrameType());
		}
	}
}
