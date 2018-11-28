package io.gridgo.connector.file;

import java.io.IOException;
import java.io.RandomAccessFile;

import io.gridgo.connector.file.engines.FileProducerEngine;
import io.gridgo.connector.file.engines.LengthPrependedFileConsumerEngine;
import io.gridgo.connector.file.engines.SimpleFileConsumerEngine;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileConsumer extends AbstractConsumer {

	private String path;

	@Getter
	private String format;

	@Getter
	private RandomAccessFile randomAccessFile;

	@Getter
	private FileProducerEngine engine;

	private boolean lengthPrepend;

	@Getter
	private byte[] buffer;

	public FileConsumer(ConnectorContext context, String path, String format, int bufferSize, boolean lengthPrepend) {
		super(context);
		this.path = path;
		this.format = format;
		this.lengthPrepend = lengthPrepend;
		this.buffer = new byte[bufferSize];
	}

	@Override
	protected void onStart() {
		try {
			this.randomAccessFile = new RandomAccessFile(path, "r");
			this.randomAccessFile.seek(0);
		} catch (IOException e) {
			throw new RuntimeException("Cannot access file", e);
		}
		readAndPublish();
	}

	private void readAndPublish() {
		var engine = lengthPrepend ? new LengthPrependedFileConsumerEngine(this) : new SimpleFileConsumerEngine(this);
		getContext().getConsumerExecutionStrategy()
				.ifPresentOrElse(strategy -> strategy.execute(engine::readAndPublish), engine::readAndPublish);
	}

	@Override
	protected void onStop() {
		try {
			this.randomAccessFile.close();
		} catch (IOException e) {
			log.warn("IOException caught when closing RandomAccessFile", e);
		}
	}

	@Override
	protected String generateName() {
		return "consumer.file." + path;
	}

	public void publishMessage(Message msg) {
		publish(msg, null);
	}
}
