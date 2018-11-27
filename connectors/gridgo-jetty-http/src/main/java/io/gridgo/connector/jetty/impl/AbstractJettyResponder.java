package io.gridgo.connector.jetty.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BType;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractResponder;
import io.gridgo.connector.jetty.HttpConstants;
import io.gridgo.connector.jetty.HttpContentTypes;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.support.DeferredAndRoutingId;
import io.gridgo.connector.jetty.support.HttpHeader;
import io.gridgo.connector.jetty.support.HttpStatus;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.wrapper.ByteBufferInputStream;
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
		return "producer.jetty.http-server." + this.uniqueIdentifier;
	}

	protected String lookUpResponseHeader(@NonNull String headerName) {
		HttpHeader httpHeader = HttpHeader.lookUp(headerName.toLowerCase());
		if (httpHeader != null && httpHeader.isForResponse() && !httpHeader.isCustom()) {
			return httpHeader.asString();
		}
		return null;
	}

	protected void writeHeaders(@NonNull BObject headers, @NonNull HttpServletResponse response) {
		for (Entry<String, BElement> entry : headers.entrySet()) {
			if (entry.getValue().isValue() && !entry.getValue().asValue().isNull()) {
				String stdHeaderName = lookUpResponseHeader(entry.getKey());
				if (stdHeaderName != null) {
					response.addHeader(stdHeaderName, entry.getValue().asValue().getString());
				}
			}
		}
	}

	@Override
	public void writeResponse(HttpServletResponse response, Message message) {
		/* -------------------------------------- */
		/**
		 * process header
		 */
		BObject headers = message.getPayload().getHeaders();

		int statusCode = headers.getInteger(HttpHeader.HTTP_STATUS.asString(), HttpStatus.OK_200.getCode());
		response.setStatus(statusCode);

		String charset = headers.getString(HttpHeader.CHARSET.asString(), "UTF-8");
		response.setCharacterEncoding(charset);

		this.writeHeaders(headers, response);

		/* ------------------------------------------ */
		/**
		 * process body
		 */
		BElement body = message.getPayload().getBody();
		if (body != null) {
			var contentType = HttpContentTypes.forValue(headers.getString(HttpConstants.CONTENT_TYPE, null));

			if (contentType == null) {
				if (body instanceof BValue) {
					contentType = HttpContentTypes.TEXT_PLAIN;
				} else if (body instanceof BReference) {
					contentType = HttpContentTypes.APPLICATION_OCTET_STREAM;
				} else {
					contentType = HttpContentTypes.APPLICATION_JSON;
				}
			}

			switch (contentType) {
			case APPLICATION_JSON:
				this.writeBodyJson(body, response);
				break;
			case APPLICATION_OCTET_STREAM:
				this.writeBodyBinary(body, response);
				break;
			case MULTIPART_FORM_DATA:
				this.writeBodyBinary(body, response);
				break;
			case TEXT_PLAIN:
			default:
				this.writeBodyTextPlain(body, response);
				break;
			}
		}
	}

	protected void takeWriter(HttpServletResponse response, Consumer<PrintWriter> writerConsumer) {
		PrintWriter writer = null;

		try {
			writer = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException("Cannot get writer from HttpSerletResponse instance");
		}

		if (writer != null) {
			try {
				writerConsumer.accept(writer);
			} finally {
				writer.flush();
			}
		}
	}

	protected void takeOutputStream(HttpServletResponse response, Consumer<OutputStream> osConsumer) {
		OutputStream outputStream = null;

		try {
			outputStream = response.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException("Cannot get writer from HttpSerletResponse instance", e);
		}

		if (outputStream != null) {
			try {
				osConsumer.accept(outputStream);
			} finally {
				try {
					outputStream.flush();
				} catch (IOException e) {
					throw new RuntimeException("Cannot flush output stream", e);
				}
			}
		}
	}

	protected void writeBodyJson(BElement body, HttpServletResponse response) {
		if (body instanceof BValue) {
			writeBodyTextPlain(body, response);
		} else if (body instanceof BReference) {
			writeBodyBinary(body, response);
		} else {
			takeWriter(response, (writer) -> body.writeJson(writer));
		}
	}

	protected void writeBodyBinary(BElement body, HttpServletResponse response) {
		final InputStream inputStream;
		if (body instanceof BReference) {
			var obj = body.asReference().getReference();
			if (obj instanceof InputStream) {
				inputStream = (InputStream) obj;
			} else if (obj instanceof ByteBuffer) {
				inputStream = new ByteBufferInputStream((ByteBuffer) obj);
			} else if (obj instanceof byte[]) {
				inputStream = new ByteArrayInputStream((byte[]) obj);
			} else {
				inputStream = null;
			}
		} else {
			inputStream = null;
		}

		takeOutputStream(response, (outputStream) -> {
			if (inputStream != null) {
				try {
					inputStream.transferTo(outputStream);
				} catch (IOException e) {
					throw new RuntimeException("Cannot transfer data from");
				}
			} else {
				body.writeBytes(outputStream);
			}
		});
	}

	protected void writePart(String name, BElement value, MultipartEntityBuilder builder) {
		name = name == null ? "" : name;
		if (value instanceof BValue) {
			if (value.getType() == BType.RAW) {
				builder.addBinaryBody(name, value.asValue().getRaw());
			} else {
				builder.addTextBody(name, value.asValue().getString());
			}
		} else if (value instanceof BReference) {
			final InputStream inputStream;
			var obj = value.asReference().getReference();
			if (obj instanceof InputStream) {
				inputStream = (InputStream) obj;
			} else if (obj instanceof ByteBuffer) {
				inputStream = new ByteBufferInputStream((ByteBuffer) obj);
			} else if (obj instanceof byte[]) {
				inputStream = new ByteArrayInputStream((byte[]) obj);
			} else {
				inputStream = null;
			}
			if (inputStream != null) {
				builder.addBinaryBody(name, inputStream);
			} else {
				throw new IllegalArgumentException("cannot make input stream from BReferrence");
			}
		} else {
			builder.addTextBody(name, value.toJson());
		}
	}

	protected void writeBodyMultipart(BElement body, HttpServletResponse response) {
		final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if (body instanceof BObject) {
			for (Entry<String, BElement> entry : body.asObject().entrySet()) {
				String name = entry.getKey();
				writePart(name, entry.getValue(), builder);
			}
		} else if (body instanceof BArray) {
			for (BElement entry : body.asArray()) {
				writePart(null, entry, builder);
			}
		} else {
			writePart(null, body, builder);
		}

		takeOutputStream(response, (outstream) -> {
			try {
				builder.build().writeTo(outstream);
			} catch (IOException e) {
				throw new RuntimeException("Cannot write multipart", e);
			}
		});
	}

	protected void writeBodyTextPlain(BElement body, HttpServletResponse response) {
		if (body instanceof BValue) {
			takeWriter(response, (writer) -> writer.write(body.asValue().getString()));
		}
	}

	@Override
	public Message generateFailureMessage(Throwable ex) {
		// print exception anyway
		getLogger().error("Error while handling request", ex);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR_500;
		BElement body = BValue.newDefault(status.getDefaultMessage());

		Payload payload = Payload.newDefault(body).addHeader(HttpHeader.HTTP_STATUS.asString(), status.getCode());
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
			} finally {
				asyncContext.complete();
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
