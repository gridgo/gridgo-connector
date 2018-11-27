package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.AbstractComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.DefaultPayload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

	@Getter
	private final ConnectorContext context;

	protected AbstractProducer(final @NonNull ConnectorContext context) {
		this.context = context;
	}

	protected Message createMessage(BObject headers, BElement body) {
		return Message.newDefault(new DefaultPayload(context.getIdGenerator().generateId(), headers, body));
	}

	protected void ack(Deferred<Message, Exception> deferred, Message response, Exception exception) {
		if (exception != null)
			ack(deferred, exception);
		else
			ack(deferred, response);
	}

	protected void ack(Deferred<Message, Exception> deferred) {
		if (deferred != null) {
			context.getCallbackInvokerStrategy().execute(() -> {
				deferred.resolve(null);
			});
		}
	}

	protected void ack(Deferred<Message, Exception> deferred, Exception exception) {
		if (deferred != null) {
			context.getCallbackInvokerStrategy().execute(() -> {
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
			context.getCallbackInvokerStrategy().execute(() -> deferred.resolve(response));
		}
	}
}
