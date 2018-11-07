package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Producer;
import io.gridgo.connector.support.exceptions.SendMessageException;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

	@Getter(AccessLevel.PROTECTED)
	private ExecutionStrategy callbackInvokeExecutor;

	@Override
	public Producer invokeCallbackOn(ExecutionStrategy strategy) {
		this.callbackInvokeExecutor = strategy;
		return this;
	}

	protected void callback(Deferred<Message, Exception> deferred, Exception exception) {
		boolean success = exception == null;
		Runnable deferredFinisher = () -> {
			if (success) {
				deferred.resolve(null);
			} else {
				deferred.reject(exception != null ? exception : new SendMessageException(exception));
			}
		};

		if (this.getCallbackInvokeExecutor() != null) {
			this.getCallbackInvokeExecutor().execute(deferredFinisher);
		} else {
			deferredFinisher.run();
		}
	}
}
