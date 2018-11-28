package io.gridgo.connector.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.joo.promise4j.Promise;

import io.gridgo.connector.file.engines.FileProducerEngine;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileProducer extends AbstractProducer {

	private File file;

	private boolean deleteOnStartup;

	private boolean deleteOnShutdown;

	private String path;

	private String mode;

	private RandomAccessFile randomAccessFile;

	@Getter
	private FileProducerEngine engine;

	public FileProducer(ConnectorContext context, String path, String mode, FileProducerEngine engine,
			boolean deleteOnStartup, boolean deleteOnShutdown) {
		super(context);
		this.engine = engine;
		this.path = path;
		this.mode = mode;
		this.deleteOnStartup = deleteOnStartup;
		this.deleteOnShutdown = deleteOnShutdown;
	}

	@Override
	protected void onStart() {
		file = new File(path);
		if (file.exists() && deleteOnStartup) {
			file.delete();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Cannot create file", e);
			}
		}
		try {
			this.randomAccessFile = new RandomAccessFile(path, mode);
			this.randomAccessFile.seek(this.randomAccessFile.length());
			this.engine.setRandomAccessFile(this.randomAccessFile);
			this.engine.start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create file", e);
		}
	}

	@Override
	protected void onStop() {
		try {
			this.engine.stop();
			this.randomAccessFile.close();
		} catch (IOException e) {
			log.warn("IOException caught when closing RandomAccessFile", e);
		}
		if (deleteOnShutdown)
			file.delete();
	}

	@Override
	protected String generateName() {
		return "producer.file:" + engine.getName() + "." + path;
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException("File doesn't support call");
	}

	@Override
	public boolean isCallSupported() {
		return false;
	}

	@Override
	public void send(Message message) {
		this.engine.send(message);
	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		return this.engine.sendWithAck(message);
	}
}
