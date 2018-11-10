package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Producer;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.generators.IdGenerator;
import io.gridgo.framework.support.impl.DefaultPayload;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

	private static final ExecutionStrategy DEFAULT_CALLBACK_EXECUTOR = new DefaultExecutionStrategy();

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private ExecutionStrategy producerExecutionStrategy = DEFAULT_CALLBACK_EXECUTOR;

	@Getter(AccessLevel.PROTECTED)
	private ExecutionStrategy callbackInvokeExecutor = DEFAULT_CALLBACK_EXECUTOR;

	@Getter
	@Setter
	private IdGenerator idGenerator;

	@Override
	public Producer invokeCallbackOn(final @NonNull ExecutionStrategy strategy) {
		this.callbackInvokeExecutor = strategy;
		return this;
	}

	@Override
	public Producer produceOn(final @NonNull ExecutionStrategy strategy) {
		this.producerExecutionStrategy = strategy;
		return this;
	}

	protected Message createMessage(BObject headers, BElement body) {
		if (idGenerator == null)
			return Message.newDefault(Payload.newDefault(headers, body));
		return Message.newDefault(new DefaultPayload(idGenerator.generateId(), headers, body));
	}

	protected void ack(Deferred<Message, Exception> deferred, Message response, Exception exception) {
		if (exception != null)
			ack(deferred, exception);
		else
			ack(deferred, response);
	}

	protected void ack(Deferred<Message, Exception> deferred) {
		if (deferred != null) {
			callbackInvokeExecutor.execute(() -> {
				deferred.resolve(null);
			});
		}
	}

	protected void ack(Deferred<Message, Exception> deferred, Exception exception) {
		if (deferred != null) {
			callbackInvokeExecutor.execute(() -> {
				if (exception == null) {
					deferred.resolve(null);
				} else {
					deferred.reject(exception);
				}
			});
		}
	}

	protected void ack(Deferred<Message, Exception> deferred, Message response) {
		if (deferred != null) {
			callbackInvokeExecutor.execute(() -> deferred.resolve(response));
		}
	}
}
