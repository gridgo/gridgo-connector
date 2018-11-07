package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Producer;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.generators.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

	private static final ExecutionStrategy DEFAULT_CALLBACK_EXECUTOR = new DefaultExecutionStrategy();

	@Getter(AccessLevel.PROTECTED)
	private ExecutionStrategy callbackInvokeExecutor = DEFAULT_CALLBACK_EXECUTOR;
	
	@Getter @Setter
	private IdGenerator idGenerator;

	@Override
	public Producer invokeCallbackOn(final @NonNull ExecutionStrategy strategy) {
		this.callbackInvokeExecutor = strategy;
		return this;
	}

	protected void ack(Deferred<Message, Exception> deferred, Exception exception) {
		if (deferred == null) {
			return;
		}
		callbackInvokeExecutor.execute(() -> {
			if (exception == null) {
				deferred.resolve(null);
			} else {
				deferred.reject(exception != null ? exception : new SendMessageException(exception));
			}
		});
	}

	protected void ack(Deferred<Message, Exception> deferred, Message response) {
		callbackInvokeExecutor.execute(() -> deferred.resolve(response));
	}
}
