package io.gridgo.socket.netty4.ws;

import java.io.IOException;
import java.nio.charset.Charset;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.NonNull;

public class Netty4WebsocketUtils {

    public static BElement parseWebsocketFrame(WebSocketFrame frame, boolean autoParse, String format) throws Exception {
        ByteBuf content = frame.content();
        if (!autoParse) {
            if (frame instanceof TextWebSocketFrame) {
                return BValue.of(content.toString(Charset.forName("UTF-8")));
            }
            if (frame instanceof BinaryWebSocketFrame) {
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                return BValue.of(bytes);
            }
        }
        try (var inputStream = new ByteBufInputStream(content)) {
            if (frame instanceof TextWebSocketFrame && format == null) {
                return BElement.ofJson(inputStream);
            }
            return BElement.ofBytes(inputStream, format);
        }
    }

    public static ChannelFuture send(@NonNull Channel channel, @NonNull BElement data) {
        return send(channel, data, Netty4WebsocketFrameType.TEXT);
    }

    public static ChannelFuture send(@NonNull Channel channel, @NonNull BElement data, @NonNull Netty4WebsocketFrameType frameType) {
        try (ByteBufOutputStream output = new ByteBufOutputStream(PooledByteBufAllocator.DEFAULT.buffer())) {
            WebSocketFrame tobeSentFrame;

            switch (frameType) {
            case BINARRY:
                data.writeBytes(output);
                tobeSentFrame = new BinaryWebSocketFrame(output.buffer());
                break;
            case TEXT:
                data.writeJson(output);
                tobeSentFrame = new TextWebSocketFrame(output.buffer());
                break;
            default:
                throw new RuntimeException("Invalid frame type " + frameType);
            }

            return channel.writeAndFlush(tobeSentFrame);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write data as text", e);
        }
    }
}
