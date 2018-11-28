package io.gridgo.connector.file.engines;

import java.io.RandomAccessFile;

import io.gridgo.bean.BArray;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.FormattedMarshallable;

public interface FileProducerEngine extends Producer, FormattedMarshallable {

	public void setRandomAccessFile(RandomAccessFile randomAccessFile);

	public long getTotalSentBytes();

	public default byte[] serialize(BArray bArray, boolean lengthPrepend) {
		var bytesToSend = serialize(bArray);
		if (!lengthPrepend)
			return bytesToSend;
		var length = bytesToSend.length;
		var bytesWithLength = new byte[length + 4];
		bytesWithLength[0] = (byte) (length >> 24);
		bytesWithLength[1] = (byte) (length >> 16);
		bytesWithLength[2] = (byte) (length >> 8);
		bytesWithLength[3] = (byte) (length /* >> 0 */);
		System.arraycopy(bytesToSend, 0, bytesWithLength, 4, length);
		return bytesWithLength;
	}
}
