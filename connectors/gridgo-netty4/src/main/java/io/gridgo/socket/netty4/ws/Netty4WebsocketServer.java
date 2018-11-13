package io.gridgo.socket.netty4.ws;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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

	protected WebSocketServerHandshakerFactory getWsFactory() {
		if (wsFactory == null) {
			synchronized (this) {
				if (wsFactory == null) {
					String proxy = this.getConfigs().getString("proxy", null);
					String wsUri = null;
					if (proxy != null) {
						wsUri = proxy;
					} else {
						wsUri = "ws://" + this.getHost().toHostAndPort() + (getPath().startsWith("/") ? "" : "/")
								+ this.getPath();
					}
					getLogger().info("handshake on wsUri: %s", wsUri);
					wsFactory = new WebSocketServerHandshakerFactory(wsUri, null, true);
				}
			}
		}
		return this.wsFactory;
	}

	@Override
	protected BElement handleIncomingMessage(long channelId, Object msg) throws Exception {
		ChannelHandlerContext ctx = getChannelHandlerContext(channelId);
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

	protected BElement handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		if (frame == null) {
			return null;
		}

		WebSocketServerHandshaker handshaker = ctx.channel().attr(HANDSHAKER_KEY).get();

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return null;
		}

		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return null;
		}

		if (frame instanceof BinaryWebSocketFrame) {
			return BElement.fromRaw(new ByteBufInputStream(((BinaryWebSocketFrame) frame).content()));
		} else if (frame instanceof TextWebSocketFrame) {
			String text = ((TextWebSocketFrame) frame).text();
			try {
				return BElement.fromJson(text);
			} catch (Exception ex) {
				return BValue.newDefault(text);
			}
		}

		return null;
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@SuppressWarnings("deprecation")
	public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// Handle a bad request.
		if (!req.decoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.method() != GET) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		if ("/".equals(req.uri())) {
			ByteBuf content = Unpooled
					.copiedBuffer("Default gridgo-netty4 connector websocket welcome page".getBytes());
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpUtil.setContentLength(res, content.readableBytes());

			sendHttpResponse(ctx, req, res);
			return;
		}

		if ("/favicon.ico".equals(req.uri())) {
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			sendHttpResponse(ctx, req, res);
			return;
		}

		// Handshake
		final WebSocketServerHandshaker handshaker = getWsFactory().newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
			ctx.channel().attr(HANDSHAKER_KEY).set(handshaker);
		}
	}

	@Override
	protected void onInitChannel(SocketChannel socketChannel) {
		ChannelPipeline pipeline = socketChannel.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
