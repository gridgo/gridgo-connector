package io.gridgo.connector.file.engines;

import java.io.RandomAccessFile;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.Setter;

public class BasicFileProducerEngine extends AbstractProducer implements FileProducerEngine {

	@Getter
	private String format;

	@Setter
	private RandomAccessFile randomAccessFile;

	private boolean lengthPrepend;

	@Getter
	private long totalSentBytes;

	public BasicFileProducerEngine(ConnectorContext context, String format, boolean lengthPrepend) {
		super(context);
		this.format = format;
		this.lengthPrepend = lengthPrepend;
	}

	@Override
	public void send(Message message) {
		doSend(message, null);
	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		var deferred = new CompletableDeferredObject<Message, Exception>();
		doSend(message, deferred);
		return deferred.promise();
	}

	private void doSend(Message message, CompletableDeferredObject<Message, Exception> deferred) {
		try {
			byte[] bytesToSend = serialize(message.getPayload().toBArray(), lengthPrepend);
			randomAccessFile.write(bytesToSend);
			totalSentBytes += bytesToSend.length;
		} catch (Exception ex) {
			ack(deferred, ex);
			return;
		}
		ack(deferred);
	}

	@Override
	protected void onStart() {

	}

	@Override
	protected void onStop() {

	}

	@Override
	protected String generateName() {
		return "basic";
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException("File doesn't support call");
	}

	@Override
	public boolean isCallSupported() {
		return false;
	}

}
