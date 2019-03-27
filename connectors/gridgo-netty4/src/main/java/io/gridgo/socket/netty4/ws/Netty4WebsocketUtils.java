package io.gridgo.socket.netty4.ws;

import static io.gridgo.socket.netty4.ws.Netty4WebsocketFrameType.TEXT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.utils.exception.RuntimeIOException;
import io.gridgo.utils.exception.UnsupportedTypeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Netty4WebsocketUtils {

    public static BElement parseWebsocketFrame(WebSocketFrame frame, boolean autoParse, String format) {
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
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static WebSocketFrame makeWebsocketFrame(@NonNull BElement data, @NonNull Netty4WebsocketFrameType frameType, String format) {
        final WebSocketFrame frame;
        if (data.isReference()) {
            var ref = data.asReference().getReference();
            if (ref == null) {
                throw new NullPointerException("Data as BReference contains null object");
            }

            if (ref instanceof WebSocketFrame) {
                return (WebSocketFrame) ref;
            }

            final ByteBuf byteBuf;
            if (ref instanceof ByteBuf) {
                byteBuf = (ByteBuf) ref;
            } else if (ref instanceof ByteBuffer) {
                byteBuf = Unpooled.wrappedBuffer((ByteBuffer) ref);
            } else if (ref instanceof InputStream || ref instanceof File) {
                InputStream inputStream = null;
                try (ByteBufOutputStream output = new ByteBufOutputStream(PooledByteBufAllocator.DEFAULT.directBuffer())) {
                    if (ref instanceof File) {
                        inputStream = new FileInputStream((File) ref);
                    } else {
                        inputStream = (InputStream) ref;
                    }
                    inputStream.transferTo(output);
                    byteBuf = output.buffer();
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                } finally {
                    if (inputStream != null && ref instanceof File) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            if (log.isWarnEnabled()) {
                                log.warn("Cannot close FileInputStream for file " + ref);
                            }
                        }
                    }
                }
            } else {
                throw new UnsupportedTypeException("Data wrapped as BReference with type " + ref.getClass() + " cannot be used in websocket frame");
            }
            frame = frameType == TEXT ? new TextWebSocketFrame(byteBuf) : new BinaryWebSocketFrame(byteBuf);
        } else {
            try (ByteBufOutputStream output = new ByteBufOutputStream(PooledByteBufAllocator.DEFAULT.directBuffer())) {
                switch (frameType) {
                case BINARRY:
                    data.writeBytes(output, format);
                    frame = new BinaryWebSocketFrame(output.buffer());
                    break;
                case TEXT:
                    if (format == null) {
                        data.writeJson(output);
                    } else {
                        data.writeBytes(output, format);
                    }
                    frame = new TextWebSocketFrame(output.buffer());
                    break;
                default:
                    throw new RuntimeException("Invalid frame type " + frameType);
                }
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
        return frame;
    }
}
