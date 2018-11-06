package io.gridgo.connector.impl;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Consumer;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class AbstractConsumer extends AbstractComponentLifecycle implements Consumer {

	@Getter(AccessLevel.PROTECTED)
	private ExecutionStrategy callbackInvokeExecutor;

	private final Collection<Object> subscribers = new CopyOnWriteArrayList<>();

	@Override
	public Consumer subscribe(BiConsumer<Message, Deferred<Message, Exception>> subscriber) {
		if (!this.subscribers.contains(subscriber)) {
			this.subscribers.add(subscriber);
		}
		return this;
	}

	@Override
	public Consumer subscribe(java.util.function.Consumer<Message> subscriber) {
		if (!this.subscribers.contains(subscriber)) {
			this.subscribers.add(subscriber);
		}
		return this;
	}

	@Override
	public Consumer invokeCallbackOn(ExecutionStrategy strategy) {
		this.callbackInvokeExecutor = strategy;
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void publish(Message message, Deferred<Message, Exception> deferred) {
		for (Object subscriber : this.subscribers) {
			if (subscriber instanceof BiConsumer<?, ?>) {
				((BiConsumer) subscriber).accept(message, deferred);
			} else if (subscriber instanceof java.util.function.Consumer<?>) {
				((java.util.function.Consumer) subscriber).accept(message);
			}
		}
	}

}
