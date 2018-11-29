package io.gridgo.connector.file.support.limit;

import java.io.IOException;
import java.nio.channels.FileChannel;

public interface FileLimitStrategy {

	public void start() throws IOException;

	public void stop() throws IOException;

	public void putBytes(long bytes) throws IOException;

	public void readWith(RandomAccessFileHandler consumer) throws IOException;

	public FileChannel getFileChannel();
}
