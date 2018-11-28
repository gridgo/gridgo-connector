package io.gridgo.connector.file.engines;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.FormattedMarshallable;

public interface FileProducerEngine extends Producer, FormattedMarshallable {

	public void setRandomAccessFile(RandomAccessFile randomAccessFile);

	public long getTotalSentBytes();

	public default long writeToFile(BElement payload, boolean lengthPrepend, ByteBuffer buffer,
			RandomAccessFile randomAccessFile) throws IOException {
		if ("raw".equals(getFormat()))
			return writeBuffer(payload, buffer, randomAccessFile, lengthPrepend);
		byte[] bytesToSend = serialize(payload);
		if (lengthPrepend)
			bytesToSend = appendWithLength(bytesToSend);
		randomAccessFile.write(bytesToSend);
		return bytesToSend.length;
	}

	public default long writeBuffer(BElement payload, ByteBuffer buffer, RandomAccessFile randomAccessFile,
			boolean lengthPrepend) throws IOException {
		buffer.clear();
		if (lengthPrepend)
			writeBytesWithLength(payload, buffer);
		else
			payload.writeBytes(buffer);
		buffer.flip();
		if (buffer.isDirect()) {
			var bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			randomAccessFile.write(bytes);
		} else {
			randomAccessFile.write(buffer.array(), 0, buffer.limit());
		}
		return buffer.limit();
	}

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

	public default void writeBytesWithLength(BElement bArray, ByteBuffer buffer) {
		buffer.position(4);
		bArray.writeBytes(buffer);
		var length = buffer.position() - 4;
		buffer.position(0);
		buffer.putInt(length);
		buffer.position(length + 4);
	}
}
