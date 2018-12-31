package io.gridgo.connector.file.support.engines;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.connector.file.support.limit.FileLimitStrategy;
import io.gridgo.connector.support.FormattedMarshallable;

public interface FileProducerEngine extends Producer, FormattedMarshallable {

    public default byte[] appendWithLength(byte[] bytesToSend) {
        var length = bytesToSend.length;
        var bytes = new byte[length + 4];
        bytes[0] = (byte) (length >> 24);
        bytes[1] = (byte) (length >> 16);
        bytes[2] = (byte) (length >> 8);
        bytes[3] = (byte) (length);
        System.arraycopy(bytesToSend, 0, bytes, 4, length);
        return bytes;
    }

    public long getTotalSentBytes();

    public void setLimitStrategy(FileLimitStrategy limitStrategy);

    public default void writeBytesWithLength(BElement bArray, ByteBuffer buffer) {
        buffer.position(4);
        bArray.writeBytes(buffer);
        var length = buffer.position() - 4;
        buffer.position(0);
        buffer.putInt(length);
        buffer.position(length + 4);
    }

    public default long writeToFile(BElement payload, boolean lengthPrepend, ByteBuffer buffer, FileChannel fileChannel) throws IOException {
        if ("raw".equals(getFormat()))
            return writeWithBuffer(payload, buffer, fileChannel, lengthPrepend);
        byte[] bytesToSend = serialize(payload);
        if (lengthPrepend)
            bytesToSend = appendWithLength(bytesToSend);
        fileChannel.write(ByteBuffer.wrap(bytesToSend));
        return bytesToSend.length;
    }

    public default long writeWithBuffer(BElement payload, ByteBuffer buffer, FileChannel channel, boolean lengthPrepend) throws IOException {
        buffer.clear();
        if (lengthPrepend)
            writeBytesWithLength(payload, buffer);
        else
            payload.writeBytes(buffer);
        buffer.flip();
        channel.write(buffer);
        return buffer.limit();
    }
}
