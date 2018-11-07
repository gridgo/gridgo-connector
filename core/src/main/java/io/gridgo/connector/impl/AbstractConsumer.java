package io.gridgo.connector.impl;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Consumer;
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

public abstract class AbstractConsumer extends AbstractComponentLifecycle implements Consumer {

	private static final ExecutionStrategy DEFAULT_CALLBACK_EXECUTOR = new DefaultExecutionStrategy();

	@Getter(AccessLevel.PROTECTED)
	private ExecutionStrategy callbackInvokeExecutor = DEFAULT_CALLBACK_EXECUTOR;

	@Getter
	private IdGenerator idGenerator;

	private final Collection<BiConsumer<Message, Deferred<Message, Exception>>> subscribers = new CopyOnWriteArrayList<>();

	@Override
	public Consumer subscribe(BiConsumer<Message, Deferred<Message, Exception>> subscriber) {
		if (!this.subscribers.contains(subscriber)) {
			this.subscribers.add(subscriber);
		}
		return this;
	}

	@Override
	public Consumer invokeCallbackOn(final @NonNull ExecutionStrategy strategy) {
		this.callbackInvokeExecutor = strategy;
		return this;
	}

	protected void publish(Message message, Deferred<Message, Exception> deferred) {
		for (BiConsumer<Message, Deferred<Message, Exception>> subscriber : this.subscribers) {
			callbackInvokeExecutor.execute(() -> subscriber.accept(message, deferred));
		}
	}

	protected Message createMessage(BObject headers, BElement body) {
		if (idGenerator == null)
			return Message.newDefault(Payload.newDefault(headers, body));
		return Message.newDefault(new DefaultPayload(idGenerator.generateId(), headers, body));
	}

	public Consumer setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		return this;
	}
}
