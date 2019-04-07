package io.gridgo.connector.jetty.impl;

import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.parser.HttpRequestParser;
import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.support.HostAndPort;
import lombok.NonNull;

public class AbstractJettyConsumer extends AbstractHasResponderConsumer implements JettyConsumer {

    private final HttpRequestParser requestParser;

    private final HostAndPort address;
    private final String path;

    private final String format;

    private JettyHttpServer httpServer;
    private Function<Throwable, Message> failureHandler;

    private final String uniqueIdentifier;

    private final Set<JettyServletContextHandlerOption> options;

    public AbstractJettyConsumer(ConnectorContext context, @NonNull HostAndPort address, boolean http2Enabled, boolean mmapEnabled, String format, String path,
            Set<JettyServletContextHandlerOption> options) {
        super(context);

        this.options = options;

        this.address = address;

        this.format = format;

        this.requestParser = HttpRequestParser.newDefault(format);

        path = (path == null || path.isBlank()) ? "/*" : path.trim();
        this.path = path.startsWith("/") ? path : ("/" + path);

        this.httpServer = JettyHttpServerManager.getInstance().getOrCreateJettyServer(this.address, http2Enabled, this.options);
        if (this.httpServer == null) {
            throw new RuntimeException("Cannot create http server for address: " + this.address);
        }

        this.uniqueIdentifier = address.toHostAndPort() + this.path;
        this.setResponder(new DefaultJettyResponder(getContext(), mmapEnabled, this.format, this.uniqueIdentifier));
    }

    protected Deferred<Message, Exception> createDeferred() {
        return new CompletableDeferredObject<>();
    }

    @Override
    protected String generateName() {
        return "consumer.jetty.http-server." + this.uniqueIdentifier;
    }

    protected JettyResponder getJettyResponder() {
        return (JettyResponder) this.getResponder();
    }

    private void onHttpRequest(HttpServletRequest request, HttpServletResponse response) {
        Message requestMessage = null;
        try {
            // parse http servlet request to message object
            requestMessage = this.requestParser.parse(request, this.options);
        } catch (Exception e) {
            getLogger().error("error while parsing http request", e);
            Message responseMessage = this.failureHandler != null ? this.failureHandler.apply(e) : this.getJettyResponder().generateFailureMessage(e);
            ((JettyResponder) this.getResponder()).writeResponse(response, responseMessage);
        }

        if (requestMessage != null) {
            var deferredAndRoutingId = getJettyResponder().registerRequest(request);
            this.publish(requestMessage.setRoutingIdFromAny(deferredAndRoutingId.getRoutingId()), deferredAndRoutingId.getDeferred());
        }
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
