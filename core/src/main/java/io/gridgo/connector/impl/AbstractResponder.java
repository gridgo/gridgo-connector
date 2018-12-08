package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.connector.Responder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public abstract class AbstractResponder extends AbstractProducer implements Responder {

	protected AbstractResponder(ConnectorContext context) {
		super(context);
	}

	protected abstract void send(Message message, Deferred<Message, Exception> deferred);

	@Override
	public final void send(@NonNull Message message) {
		if (message.getRoutingId() == null || !message.getRoutingId().isPresent()) {
			throw new IllegalArgumentException("Message must contain not-null routingId");
		}
		this.send(message, null);
	}

	@Override
	public final Promise<Message, Exception> sendWithAck(@NonNull Message message) {
		if (message.getRoutingId() == null) {
			throw new IllegalArgumentException("Message must contain not-null routingId");
		}
		Deferred<Message, Exception> deferred = createDeferred();
		this.send(message, deferred);
		return deferred.promise();
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new AsyncDeferredObject<>();
	}

	@Override
	public final Promise<Message, Exception> call(Message request) {
		return Responder.super.call(request);
	}

	@Override
	protected void onStart() {
		// responder donot need to be started
	}

	@Override
	protected void onStop() {
		// no need to stop a responder
	}
}
