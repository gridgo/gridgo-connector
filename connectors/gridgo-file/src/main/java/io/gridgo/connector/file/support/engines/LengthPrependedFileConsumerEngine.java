package io.gridgo.connector.file.support.engines;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedList;

import io.gridgo.connector.file.FileConsumer;
import io.gridgo.connector.file.support.exceptions.LengthMismatchException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LengthPrependedFileConsumerEngine implements FileConsumerEngine {

	private FileConsumer fileConsumer;

	private Collection<File> files;

	public LengthPrependedFileConsumerEngine(FileConsumer fileConsumer) throws FileNotFoundException {
		this.fileConsumer = fileConsumer;
		this.files = initFiles();
	}

	private Collection<File> initFiles() throws FileNotFoundException {
		var list = new LinkedList<File>();
		if (this.fileConsumer.isHasRotation()) {
			var count = this.fileConsumer.getCount();
			for (int i = count - 1; i >= 1; i--) {
				var file = new File(this.fileConsumer.getPath() + "." + (i - 1));
				if (file.exists())
					list.add(file);
			}
		}
		list.add(new File(this.fileConsumer.getPath()));
		return list;
	}

	@Override
	public void readAndPublish() {
		var buffer = this.fileConsumer.getBuffer();
		for (var file : files) {
			try (var randomAccessFile = new RandomAccessFile(file, "r")) {
				buffer = readAndPublish(buffer, randomAccessFile);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private byte[] readAndPublish(byte[] buffer, RandomAccessFile randomAccessFile) throws IOException {
		while (true) {
			var length = tryGetLength(randomAccessFile);
			if (length == -1)
				break;
			if (length > buffer.length) {
				log.warn("Buffer overflow detected. Limit: %d. Required: %d", buffer.length, length);
				buffer = new byte[length];
			}
			int read = randomAccessFile.read(buffer, 0, length);
			if (read != length)
				throw new LengthMismatchException(length, read);
			var payload = deserialize(buffer, length);
			var msg = Message.parse(payload);
			if (msg instanceof MultipartMessage) {
				var messages = ((MultipartMessage) msg).buildOriginalMessages();
				for (var message : messages) {
					this.fileConsumer.publishMessage(message);
				}
			} else {
				this.fileConsumer.publishMessage(msg);
			}
		}
		return buffer;
	}

	private int tryGetLength(RandomAccessFile randomAccessFile) throws IOException {
		try {
			return randomAccessFile.readInt();
		} catch (EOFException ex) {
			return -1;
		}
	}

	@Override
	public String getFormat() {
		return this.fileConsumer.getFormat();
	}
}
