package io.gridgo.connector.jetty.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractResponder;
import io.gridgo.connector.jetty.DeferredAndRoutingId;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.jetty.support.HttpHeaders;
import io.gridgo.jetty.support.HttpStatus;
import lombok.NonNull;

public class AbstractJettyResponder extends AbstractResponder implements JettyResponder {

	private static final AtomicLong ID_SEED = new AtomicLong(0);

	private final Map<Long, Deferred<Message, Exception>> deferredResponses = new NonBlockingHashMap<>();
	private Function<Throwable, Message> failureHandler = this::generateFailureMessage;
	private final String uniqueIdentifier;

	protected AbstractJettyResponder(ConnectorContext context, @NonNull String uniqueIdentifier) {
		super(context);
		this.uniqueIdentifier = uniqueIdentifier;
	}

	@Override
	protected void send(Message message, Deferred<Message, Exception> deferredAck) {
		try {
			if (message.getRoutingId().isPresent()) {
				long routingId = message.getRoutingId().get().getLong();
				var deferredResponse = this.deferredResponses.get(routingId);
				if (deferredResponse != null) {
					deferredResponse.resolve(message);
					this.ack(deferredAck);
				} else {
					this.ack(deferredAck, new RuntimeException("Cannot find deferred for routing id: " + routingId));
				}
			} else {
				this.ack(deferredAck, new RuntimeException("Routing id must be provided"));
			}
		} catch (Exception e) {
			deferredAck.reject(e);
		}
	}

	@Override
	protected String generateName() {
		return "producer.jetty." + this.uniqueIdentifier;
	}

	@Override
	public void writeResponse(HttpServletResponse response, Message responseMessage) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			Consumer<Throwable> exceptionHandler = this.getContext().getExceptionHandler();
			if (exceptionHandler != null) {
				exceptionHandler.accept(e);
			} else {
				getLogger().error("Error while get response's writer", e);
			}
		}

		if (writer != null) {
			BObject headers = responseMessage.getPayload().getHeaders();
			HttpHeaders.writeHeaders(headers, response);
			BElement body = responseMessage.getPayload().getBody();
			if (body != null) {
				body.writeJson(writer);
			} else {
				writer.write("");
			}
			writer.flush();
		}
	}

	@Override
	public Message generateFailureMessage(Throwable ex) {
		// print exception anyway
		getLogger().error("Error while handling request", ex);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR_500;
		BElement body = BValue.newDefault(status.getDefaultMessage());

		Payload payload = Payload.newDefault(body).addHeader(HttpHeaders.STATUS_CODE, status.getCode());
		return Message.newDefault(payload);
	}

	@Override
	public DeferredAndRoutingId registerRequest(@NonNull HttpServletRequest request) {
		final Deferred<Message, Exception> deferredResponse = new CompletableDeferredObject<>();
		final AsyncContext asyncContext = request.startAsync();
		final long routingId = ID_SEED.getAndIncrement();
		this.deferredResponses.put(routingId, deferredResponse);
		deferredResponse.promise().always((stt, resp, ex) -> {
			try {
				HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
				Message responseMessage = (stt == DeferredStatus.RESOLVED) ? resp : failureHandler.apply(ex);
				writeResponse(response, responseMessage);
				asyncContext.complete();
			} finally {
				deferredResponses.remove(routingId);
			}
		});
		return DeferredAndRoutingId.builder().deferred(deferredResponse).routingId(routingId).build();
	}

	@Override
	public JettyResponder setFailureHandler(Function<Throwable, Message> failureHandler) {
		this.failureHandler = failureHandler;
		return this;
	}
}
