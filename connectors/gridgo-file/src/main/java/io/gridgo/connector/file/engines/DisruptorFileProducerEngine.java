package io.gridgo.connector.file.engines;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

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

	private ByteBuffer buffer;

	private FileChannel channel;

	public DisruptorFileProducerEngine(ConnectorContext context, String format, int bufferSize, int ringBufferSize,
			boolean batchingEnabled, int maxBatchSize, boolean lengthPrepend) {
		super(context, ringBufferSize, batchingEnabled, maxBatchSize);
		this.buffer = ByteBuffer.allocateDirect(bufferSize);
		this.format = format;
		this.lengthPrepend = lengthPrepend;
	}

	@Override
	protected void onStart() {
		this.totalSentBytes = 0;
		this.channel = randomAccessFile.getChannel();

		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void executeSendOnSingleThread(Message message) throws Exception {
		totalSentBytes += writeToFile(message.getPayload().toBArray(), lengthPrepend, buffer, channel);
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
