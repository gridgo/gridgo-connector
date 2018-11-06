package io.gridgo.socket.producer;

import java.nio.ByteBuffer;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleDeferredObject;

import com.lmax.disruptor.dsl.Disruptor;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.utils.helper.Loggable;

public class AbstractSocketProducer extends AbstractProducer implements SocketProducer, Loggable {

	private final ByteBuffer buffer = ByteBuffer.allocateDirect(128 * 1024);

	private final Socket socket;

	private Disruptor<ProducerEvent> sendWorker;

	protected AbstractSocketProducer(Socket socket) {
		this.socket = socket;

		this.sendWorker = new Disruptor<>(ProducerEvent::new, 1024, (runnable) -> {
			return new Thread(runnable);
		});
		this.sendWorker.handleEventsWith(this::executeSend);
	}

	private void executeSend(ProducerEvent event, long sequence, boolean endOfBatch) {
		buffer.clear();
		event.getData().writeBytes(buffer);
		buffer.flip();
		int sendResult = -1;
		Exception e = null;
		try {
			sendResult = this.socket.send(buffer);
		} catch (Exception ex) {
			e = ex;
		}

		Deferred<Message, Exception> deferred = event.getDeferred();
		if (deferred != null) {
			if (sendResult == 0) {
				deferred.resolve(null);
			} else {
				deferred.reject(e != null ? e : new SendMessageException(e));
			}
		}
	}

	private void _send(Message message, Deferred<Message, Exception> deferred) {
		final Payload payload = message.getPayload();
		this.sendWorker.publishEvent((ProducerEvent event, long sequence) -> {
			event.clear();
			event.setDeferred(deferred);
			event.getData().addAnySequence(payload.getId(), payload.getHeaders(), payload.getBody());
		});
	}

	@Override
	public void send(Message message) {
		if (!this.isStarted()) {
			return;
		}
		this._send(message, null);
	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		Deferred<Message, Exception> deferred = new SimpleDeferredObject<>(null, null);
		this._send(message, deferred);
		return deferred.promise();
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Consumer invokeCallbackOn(ExecutionStrategy strategy) {
		return null;
	}

	@Override
	protected void onStart() {
		this.sendWorker.start();
	}

	@Override
	protected void onStop() {
		this.sendWorker.shutdown();
	}

}
