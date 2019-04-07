package io.gridgo.socket.netty4.ws;

import static io.gridgo.socket.netty4.ws.Netty4WebsocketFrameType.TEXT;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.impl.AbstractNetty4SocketServer;
import io.gridgo.socket.netty4.utils.SSLContextRegistry;
import io.gridgo.utils.support.HostAndPort;
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
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class Netty4WebsocketServer extends AbstractNetty4SocketServer implements Netty4Websocket {

    private boolean ssl = false;
    private SSLContext sslContext;

    private boolean autoParse = true;
    private String format = null;

    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER = AttributeKey.newInstance("handshaker");

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

    public Netty4WebsocketServer() {
        this(false, null);
    }

    /**
     * Create new Netty4WebsocketServer instance
     * 
     * @param useSSL         whether using ssl or not
     * @param sslContextName which registered with SSLContextRegistry.getInstance()
     */
    public Netty4WebsocketServer(boolean useSSL, String sslContextName) {
        this.ssl = useSSL;
        if (this.ssl) {
            this.sslContext = SSLContextRegistry.getInstance().lookupMandatory(sslContextName);
        }
    }

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private String path;

    @Getter
    private Netty4WebsocketFrameType frameType = TEXT;

    @Getter(AccessLevel.PROTECTED)
    private WebSocketServerHandshakerFactory wsFactory;

    private HostAndPort host;

    @Override
    protected void onApplyConfig(@NonNull String name) {
        super.onApplyConfig(name);
        switch (name.trim().toLowerCase()) {
        case "autoParse":
            this.autoParse = this.getConfigs().getBoolean(name, this.autoParse);
            break;
        case "format":
            this.format = this.getConfigs().getString(name, this.format);
            break;
        }
    }

    @Override
    protected void closeChannel(Channel channel) {
        channel.writeAndFlush(new CloseWebSocketFrame());
        super.closeChannel(channel);
    }

    protected String getWsUri() {
        String proxy = this.getConfigs().getString("proxy", null);

        if (proxy != null) {
            return proxy;
        }

        int port = host.getPortOrDefault(ssl ? 443 : 80);
        StringBuilder sb = new StringBuilder();
        return sb.append(ssl ? "wss" : "ws") //
                 .append("://") //
                 .append(host.getHostOrDefault("localhost")) //
                 .append(port == 80 ? "" : (":" + port)) //
                 .append(getPath().startsWith("/") ? "" : "/") //
                 .append(this.getPath()) //
                 .toString();
    }

    @SuppressWarnings("deprecation")
    private void handleHttpRequest(Channel channel, FullHttpRequest req) {
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

        // response for root path
        if ("/".equals(req.uri())) {
            ByteBuf content = Unpooled.copiedBuffer("Default gridgo-socket-netty4 connector websocket welcome page".getBytes());
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpUtil.setContentLength(res, content.readableBytes());

            sendHttpResponse(channel, req, res);
            return;
        }

        // response for favicon.ico, default not supported
        if ("/favicon.ico".equals(req.uri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(channel, req, res);
            return;
        }

        // put all header entries to channel details
        var channelDetails = getChannelDetails(extractChannelId(channel));
        req.headers().forEach(entry -> {
            channelDetails.put(entry.getKey(), entry.getValue());
        });

        // Handshake
        final WebSocketServerHandshaker handshaker = getWsFactory().newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            handshaker.handshake(channel, req);
            channel.attr(HANDSHAKER).set(handshaker);
        }
    }

    @Override
    protected BElement handleIncomingMessage(String channelId, Object msg) throws Exception {
        Channel channel = getChannel(channelId);
        if (channel != null) {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(channel, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                return handleWebSocketFrame(channel, (WebSocketFrame) msg);
            }
        }
        return null;
    }

    protected BElement handleWebSocketFrame(Channel channel, WebSocketFrame frame) throws Exception {
        if (frame == null) {
            return null;
        }

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
//			System.out.println("[ws server] - Got close websocket frame from client, close channel ");
            WebSocketServerHandshaker handshaker = channel.attr(HANDSHAKER).get();
            handshaker.close(channel, (CloseWebSocketFrame) frame.retain());
            return null;
        }

        if (frame instanceof PingWebSocketFrame) {
            channel.write(new PongWebSocketFrame(frame.content().retain()));
            return null;
        }

        return Netty4WebsocketUtils.parseWebsocketFrame(frame, this.autoParse, this.format);
    }

    @Override
    protected void onBeforeBind(HostAndPort host) {
        this.host = host;
        wsFactory = new WebSocketServerHandshakerFactory(getWsUri(), null, true);

        String configFrameType = this.getConfigs().getString("frameType", "text");
        this.frameType = Netty4WebsocketFrameType.fromNameOrDefault(configFrameType, TEXT);
    }

    @Override
    protected void onInitChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (this.ssl) {
            final SSLEngine sslEngine = this.sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            pipeline.addLast(new SslHandler(sslEngine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
    }

    @Override
    public ChannelFuture send(String channelId, BElement data) {
        Channel channel = this.getChannel(channelId);
        if (data == null) {
            closeChannel(channel);
            return null;
        } else {
            return channel.writeAndFlush(Netty4WebsocketUtils.makeWebsocketFrame(data, getFrameType(), this.format));
        }
    }
}
