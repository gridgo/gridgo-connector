package io.gridgo.connector.file;

import java.io.FileNotFoundException;

import io.gridgo.connector.file.support.engines.LengthPrependedFileConsumerEngine;
import io.gridgo.connector.file.support.engines.SimpleFileConsumerEngine;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public class FileConsumer extends AbstractConsumer {

	@Getter
	private String path;

	@Getter
	private String format;

	private boolean lengthPrepend;

	@Getter
	private byte[] buffer;

	@Getter
	private boolean hasRotation;

	@Getter
	private int count;

	public FileConsumer(ConnectorContext context, String path, String format, int bufferSize, boolean lengthPrepend,
			boolean hasRotation, int count) {
		super(context);
		this.path = path;
		this.format = format;
		this.lengthPrepend = lengthPrepend;
		this.buffer = new byte[bufferSize];
		this.hasRotation = hasRotation;
		this.count = count;
	}

	@Override
	protected void onStart() {
		readAndPublish();
	}

	private void readAndPublish() {
		try {
			var engine = lengthPrepend ? new LengthPrependedFileConsumerEngine(this)
					: new SimpleFileConsumerEngine(this);
			getContext().getConsumerExecutionStrategy()
					.ifPresentOrElse(strategy -> strategy.execute(engine::readAndPublish), engine::readAndPublish);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void onStop() {

	}

	@Override
	protected String generateName() {
		return "consumer.file." + path;
	}

	public void publishMessage(Message msg) {
		publish(msg, null);
	}
}
