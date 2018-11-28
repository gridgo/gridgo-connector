package io.gridgo.connector.file.engines;

import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Objects;

import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;
import lombok.Getter;
import lombok.Setter;

public class DisruptorFileProducerEngine extends SingleThreadSendingProducer implements FileProducerEngine {

	private boolean lengthPrepend;

	@Getter
	private String format;

	@Getter
	private long totalSentBytes;

	@Setter
	private RandomAccessFile randomAccessFile;

	public DisruptorFileProducerEngine(ConnectorContext context, String format, int ringBufferSize,
			boolean batchingEnabled, int maxBatchSize, boolean lengthPrepend) {
		super(context, ringBufferSize, batchingEnabled, maxBatchSize);
		this.format = format;
		this.lengthPrepend = lengthPrepend;
	}

	@Override
	protected void onStart() {
		Objects.requireNonNull(this.randomAccessFile);

		this.totalSentBytes = 0;
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void executeSendOnSingleThread(Message message) throws Exception {
		byte[] bytesToSend = serialize(message.getPayload().toBArray(), lengthPrepend);
		randomAccessFile.write(bytesToSend);
		totalSentBytes += bytesToSend.length;
	}

	@Override
	protected Message accumulateBatch(Collection<Message> messages) {
		return new MultipartMessage(messages);
	}

	@Override
	protected String generateName() {
		return "disruptor";
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
