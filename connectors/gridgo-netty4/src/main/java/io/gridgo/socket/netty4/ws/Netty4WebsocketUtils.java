package io.gridgo.socket.netty4.ws;

import java.io.IOException;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.NonNull;

public class Netty4WebsocketUtils {

    public static BElement parseWebsocketFrame(WebSocketFrame frame) {
        if (frame instanceof BinaryWebSocketFrame) {
            return BElement.ofBytes(new ByteBufInputStream(((BinaryWebSocketFrame) frame).content()));
        } else if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            try {
                return BElement.ofJson(text);
            } catch (Exception ex) {
                return BValue.of(text);
            }
        }
        return null;
    }

    public static ChannelFuture send(@NonNull Channel channel, @NonNull BElement data) {
        return send(channel, data, Netty4WebsocketFrameType.TEXT);
    }

    public static ChannelFuture send(@NonNull Channel channel, @NonNull BElement data,
            @NonNull Netty4WebsocketFrameType frameType) {
        try (ByteBufOutputStream output = new ByteBufOutputStream(Unpooled.buffer())) {
            WebSocketFrame tobeSentFrame;

            switch (frameType) {
            case BINARRY:
                data.writeBytes(output);
                tobeSentFrame = new BinaryWebSocketFrame(output.buffer());
                break;
            case TEXT:
                output.write(data.toJson().getBytes());
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
