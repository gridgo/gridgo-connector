package io.gridgo.connector.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import lombok.Getter;

public class FileRotater {

	private String basePath;

	private String mode;

	private long limit;

	private int count;

	private RandomAccessFile file;

	@Getter
	private FileChannel fileChannel;

	private long written = -1;

	private File[] files;

	private boolean deleteOnStartup;

	private boolean deleteOnShutdown;

	public FileRotater(String basePath, String mode, long limit, int count, boolean deleteOnStartup,
			boolean deleteOnShutdown) throws IOException {
		this.basePath = basePath;
		this.mode = mode;
		this.limit = limit;
		this.count = count;
		this.deleteOnStartup = deleteOnStartup;
		this.deleteOnShutdown = deleteOnShutdown;
		this.files = initFiles();
	}

	public void start() throws IOException {
		if (deleteOnStartup)
			deleteFiles();
		resetFile();
	}

	public void stop() throws IOException {
		closeFile();
		if (deleteOnShutdown)
			deleteFiles();
	}

	private void deleteFiles() {
		for (var file : files) {
			file.delete();
		}
	}

	private File[] initFiles() {
		var files = new File[count];
		files[0] = new File(basePath);
		for (int i = 1; i < count; i++) {
			files[i] = new File(basePath + "." + (i - 1));
		}
		return files;
	}

	private void closeFile() throws IOException {
		this.file.close();
	}

	private void resetFile() throws IOException {
		this.file = new RandomAccessFile(basePath, mode);
		var length = this.file.length();
		this.file.seek(length);
		this.written = length;
		this.fileChannel = this.file.getChannel();
	}

	public void putBytes(long bytes) throws IOException {
		this.written += bytes;
		if (this.limit > 0 && this.written > this.limit) {
			rotate();
		}
	}

	private void rotate() throws IOException {
		closeFile();
		for (int i = count - 2; i >= 0; i--) {
			File f1 = files[i];
			File f2 = files[i + 1];
			if (f1.exists()) {
				if (f2.exists())
					f2.delete();
				f1.renameTo(f2);
			}
		}
		resetFile();
	}
}
