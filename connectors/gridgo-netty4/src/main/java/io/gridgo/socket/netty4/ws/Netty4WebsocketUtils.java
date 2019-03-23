package io.gridgo.socket.netty4.ws;

import java.io.IOException;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.utils.exception.UnsupportedTypeException;
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

    public static BElement parseWebsocketFrame(WebSocketFrame frame, boolean autoParseAsBElement, String serializerName) throws Exception {
        if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf content = frame.content();
            if (!autoParseAsBElement) {
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                return BValue.of(bytes);
            }
            try (var inputStream = new ByteBufInputStream(content)) {
                return BElement.ofBytes(inputStream, serializerName);
            }
        } else if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            if (!autoParseAsBElement) {
                return BValue.of(text);
            }
            if (serializerName == null) {
                return BElement.ofJson(text);
            } else {
                try (var inputStream = new ByteBufInputStream(frame.content())) {
                    return BElement.ofBytes(inputStream, serializerName);
                }
            }
        }
        throw new UnsupportedTypeException("Unexpected websocket frame type: " + frame.getClass().getName());
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
