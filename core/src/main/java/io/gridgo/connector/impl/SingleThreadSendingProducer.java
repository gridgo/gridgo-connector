package io.gridgo.connector.impl;

import java.util.concurrent.ThreadFactory;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import com.lmax.disruptor.dsl.Disruptor;

import io.gridgo.framework.support.Message;

public abstract class SingleThreadSendingProducer extends AbstractProducer {

	private final Disruptor<ProducerEvent> sendWorker;

	protected SingleThreadSendingProducer(int ringBufferSize, ThreadFactory threadFactory) {
		this.sendWorker = new Disruptor<>(ProducerEvent::new, ringBufferSize, threadFactory);
		this.sendWorker.handleEventsWith(this::handleSend);
	}

	protected SingleThreadSendingProducer(int ringBufferSize) {
		this(ringBufferSize, (runnable) -> {
			return new Thread(runnable);
		});
	}

	@Override
	protected void onStart() {
		this.sendWorker.start();
	}

	@Override
	protected void onStop() {
		this.sendWorker.shutdown();
	}

	private void handleSend(ProducerEvent event, long sequence, boolean endOfBatch) {
		try {
			this.executeSendOnSingleThread(event.getMessage());
		} catch (Exception e) {
			this.ack(event.getDeferred(), e);
		}
	}

	protected abstract void executeSendOnSingleThread(Message message) throws Exception;

	private void _send(Message message, Deferred<Message, Exception> deferred) {
		this.sendWorker.publishEvent((ProducerEvent event, long sequence) -> {
			event.clear();
			event.setDeferred(deferred);
			event.setMessage(message);
		});
	}

	@Override
	public final void send(Message message) {
		if (!this.isStarted()) {
			return;
		}
		this._send(message, null);
	}

	protected abstract Deferred<Message, Exception> createDeferred();

	@Override
	public final Promise<Message, Exception> sendWithAck(Message message) {
		Deferred<Message, Exception> deferred = createDeferred();
		this._send(message, deferred);
		return deferred.promise();
	}
}
