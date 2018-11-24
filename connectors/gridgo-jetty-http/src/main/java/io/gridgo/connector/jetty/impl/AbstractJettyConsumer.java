package io.gridgo.connector.jetty.impl;

import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.jetty.JettyHttpServer;
import io.gridgo.jetty.JettyHttpServerManager;
import io.gridgo.jetty.support.HttpMessageHelper;
import io.gridgo.utils.support.HostAndPort;
import lombok.NonNull;

public class AbstractJettyConsumer extends AbstractHasResponderConsumer implements JettyConsumer {

	private final String httpType;
	private final HostAndPort address;
	private final String path;

	private JettyHttpServer httpServer;
	private Function<Throwable, Message> failureHandler;

	private final String uniqueIdentifier;

	public AbstractJettyConsumer(ConnectorContext context, @NonNull HostAndPort address, String path, String httpType) {
		super(context);

		this.httpType = (httpType == null || httpType.isBlank()) ? "http" : httpType;
		if (!HTTP_SERVER_TYPES.contains(this.httpType)) {
			throw new IllegalArgumentException("Http type is invalid, allowed: " + HTTP_SERVER_TYPES);
		}

		this.address = address;

		path = (path == null || path.isBlank()) ? "/*" : path.trim();
		this.path = path.startsWith("/") ? path : ("/" + path);

		this.httpServer = JettyHttpServerManager.getInstance().getOrCreateJettyServer(this.address);
		if (this.httpServer == null) {
			throw new RuntimeException("Cannot create http server for address: " + this.address);
		}

		this.uniqueIdentifier = this.httpType + "://" + address.toHostAndPort() + this.path;
		this.setResponder(new DefaultJettyResponder(getContext(), this.uniqueIdentifier));
	}

	@Override
	protected String generateName() {
		return "consumer.jetty." + this.uniqueIdentifier;
	}

	protected JettyResponder getJettyResponder() {
		return (JettyResponder) this.getResponder();
	}

	private void onHttpRequest(HttpServletRequest request, HttpServletResponse response) {
		Message requestMessage = null;
		try {
			// parse http servlet request to message object
			requestMessage = HttpMessageHelper.parse(request);
		} catch (Exception e) {
			Message responseMessage = this.failureHandler != null ? this.failureHandler.apply(e)
					: this.getJettyResponder().generateFailureMessage(e);
			((JettyResponder) this.getResponder()).writeResponse(response, responseMessage);
		}

		if (requestMessage != null) {
			var deferred = ((JettyResponder) this.getResponder()).registerRequest(request);
			this.publish(requestMessage, deferred);
		}
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new CompletableDeferredObject<>();
	}

	@Override
	protected void onStart() {
		this.httpServer.start();
		this.httpServer.addPathHandler(this.path, this::onHttpRequest);
	}

	@Override
	protected void onStop() {
		this.httpServer.stop();
	}

	@Override
	public JettyConsumer setFailureHandler(Function<Throwable, Message> failureHandler) {
		this.failureHandler = failureHandler;
		getJettyResponder().setFailureHandler(failureHandler);
		return this;
	}

}
