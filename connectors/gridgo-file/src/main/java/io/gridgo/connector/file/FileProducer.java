package io.gridgo.connector.file;

import java.io.IOException;

import org.joo.promise4j.Promise;

import io.gridgo.connector.file.support.engines.FileProducerEngine;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public class FileProducer extends AbstractProducer {

	private boolean deleteOnStartup;

	private boolean deleteOnShutdown;

	private String path;

	private String mode;

	@Getter
	private FileProducerEngine engine;

	private long limit;

	private int count;

	public FileProducer(ConnectorContext context, String path, String mode, FileProducerEngine engine,
			boolean deleteOnStartup, boolean deleteOnShutdown, long limit, int count) {
		super(context);
		this.engine = engine;
		this.path = path;
		this.mode = mode;
		this.deleteOnStartup = deleteOnStartup;
		this.deleteOnShutdown = deleteOnShutdown;
		this.limit = limit;
		this.count = count;
	}

	@Override
	protected void onStart() {
		try {
			var rotater = new FileRotater(path, mode, limit, count, deleteOnStartup, deleteOnShutdown);
			this.engine.setRotater(rotater);
			this.engine.start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create file", e);
		}
	}

	@Override
	protected void onStop() {
		this.engine.stop();
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
