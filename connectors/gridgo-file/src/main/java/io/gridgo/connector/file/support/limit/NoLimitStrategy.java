package io.gridgo.connector.file.support.limit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import lombok.Getter;

public class NoLimitStrategy implements FileLimitStrategy {

	private String path;

	private String mode;

	private boolean deleteOnStartup;

	private boolean deleteOnShutdown;

	@Getter
	private FileChannel fileChannel;

	private File file;

	private RandomAccessFile raf;

	private boolean override;

	public NoLimitStrategy(String path, String mode, boolean deleteOnStartup, boolean deleteOnShutdown,
			boolean override) {
		this.path = path;
		this.mode = mode;
		this.deleteOnStartup = deleteOnStartup;
		this.deleteOnShutdown = deleteOnShutdown;
		this.override = override;
	}

	@Override
	public void start() throws IOException {
		this.file = new File(path);
		if (deleteOnStartup)
			this.file.delete();
		this.raf = new RandomAccessFile(file, mode);
		this.fileChannel = raf.getChannel();
		if (!override)
			this.raf.seek(this.raf.length());
	}

	@Override
	public void stop() throws IOException {
		this.raf.close();
		if (deleteOnShutdown)
			this.file.delete();
	}

	@Override
	public void putBytes(long bytes) throws IOException {

	}

	@Override
	public void readWith(RandomAccessFileHandler consumer) throws IOException {
		if (!this.file.exists())
			return;
		try (var raf = new RandomAccessFile(this.file, "r")) {
			consumer.process(raf);
		}
	}
}
