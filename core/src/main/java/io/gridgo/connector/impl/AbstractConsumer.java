package io.gridgo.connector.impl;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractConsumer extends AbstractComponentLifecycle implements Consumer {

	private final Collection<BiConsumer<Message, Deferred<Message, Exception>>> subscribers = new CopyOnWriteArrayList<>();

	@Getter
	private final ConnectorContext context;

	public AbstractConsumer(ConnectorContext context) {
		this.context = context;
	}

	@Override
	public Consumer subscribe(BiConsumer<Message, Deferred<Message, Exception>> subscriber) {
		if (!this.subscribers.contains(subscriber)) {
			this.subscribers.add(subscriber);
		}
		return this;
	}

	@Override
	public void clearSubscribers() {
		this.subscribers.clear();
	}

	protected void publish(@NonNull Message message, Deferred<Message, Exception> deferred) {
		if (!message.getMisc().containsKey("source")) {
			message.addMisc("source", this.getName());
		}
		for (var subscriber : this.subscribers) {
			try {
				context.getCallbackInvokerStrategy().execute(() -> subscriber.accept(message, deferred));
			} catch (Exception ex) {
				if (deferred != null) {
					deferred.reject(ex);
				}
			}
		}
	}

	/**
	 * check if message not null, message's payload not null, message's payload id
	 * is empty, then set message's payload id by value generated from idGenerator
	 * if presented
	 * 
	 * @param message the message where to take payload
	 */
	protected void ensurePayloadId(Message message) {
		if (message != null) {
			ensurePayloadId(message.getPayload());
		}
	}

	/**
	 * check if payload not null, payload's id is empty, then set payload's id by
	 * value generated from idGenerator if presented
	 * 
	 * @param payload
	 */
	protected void ensurePayloadId(Payload payload) {
		if (payload != null && payload.getId().isEmpty() && context.getIdGenerator().isPresent()) {
			payload.setId(context.getIdGenerator().get().generateId());
		}
	}

	/**
	 * create a message with payload which contains the headers and body, auto id
	 * generated
	 * 
	 * @param headers payload's headers
	 * @param body    payload's body
	 * @return the message
	 */
	protected Message createMessage(BObject headers, BElement body) {
		Payload payload = null;
		if (headers != null || body != null) {
			payload = Payload.newDefault(headers, body);
		}
		this.ensurePayloadId(payload);
		return Message.newDefault(payload);
	}

	/**
	 * create a message with empty payload's header, auto id generated
	 * 
	 * @param payload's body
	 * @return the message
	 */
	protected Message createMessage(BElement body) {
		return createMessage(null, body);
	}

	/**
	 * create a message without payload (message.getPayload() == null) auto id
	 * generated
	 * 
	 * @return the message
	 */
	protected Message createMessage() {
		return createMessage(null);
	}

	protected Message parseMessage(BElement data) {
		Message msg = Message.parse(data);
		ensurePayloadId(msg);
		return msg;
	}

	protected Message parseMessage(byte[] data) {
		Message msg = Message.parse(data);
		ensurePayloadId(msg);
		return msg;
	}

	protected Message parseMessage(ByteBuffer data) {
		Message msg = Message.parse(data);
		ensurePayloadId(msg);
		return msg;
	}

	protected Message parseMessage(InputStream data) {
		Message msg = Message.parse(data);
		ensurePayloadId(msg);
		return msg;
	}
}
