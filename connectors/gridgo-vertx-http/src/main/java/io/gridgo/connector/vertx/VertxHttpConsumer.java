package io.gridgo.connector.vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.SimpleDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.bean.impl.BFactory;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.ConnectionRef;
import io.gridgo.connector.vertx.support.exceptions.UnsupportedFormatException;
import io.gridgo.framework.support.Message;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class VertxHttpConsumer extends AbstractConsumer implements Consumer {

	private static final String DEFAULT_FORMAT = "json";

	private static final Map<String, ConnectionRef<ServerRouterTuple>> SERVER_MAP = new HashMap<>();

	private VertxOptions vertxOptions;

	private HttpServerOptions httpOptions;

	private ConnectionRef<ServerRouterTuple> connRef;

	private String path;

	private String method;

	private String format;

	private Function<Throwable, Message> failureHandler;

	public VertxHttpConsumer(VertxOptions vertxOptions, HttpServerOptions options) {
		this.vertxOptions = vertxOptions;
		this.httpOptions = options;
	}

	public VertxHttpConsumer(VertxOptions vertxOptions, HttpServerOptions options, String path, String method) {
		this.vertxOptions = vertxOptions;
		this.httpOptions = options;
		this.path = path;
		this.method = method;
		this.format = DEFAULT_FORMAT;
	}

	public VertxHttpConsumer(VertxOptions vertxOptions, HttpServerOptions options, String path, String method,
			String format) {
		this.vertxOptions = vertxOptions;
		this.httpOptions = options;
		this.path = path;
		this.method = method;
		this.format = format;
	}

	@Override
	protected void onStart() {
		String connectionKey = httpOptions.getHost() + ":" + httpOptions.getPort();
		synchronized (SERVER_MAP) {
			if (SERVER_MAP.containsKey(connectionKey)) {
				connRef = SERVER_MAP.get(connectionKey);
			} else {
				Vertx vertx = Vertx.vertx(vertxOptions);
				HttpServer server = vertx.createHttpServer(httpOptions);
				Router router = Router.router(vertx);
				server.requestHandler(router::accept);
				connRef = new ConnectionRef<>(new ServerRouterTuple(vertx, server, router));
				SERVER_MAP.put(connectionKey, connRef);
				server.listen();
			}
			connRef.ref();
		}

		configureRouter(connRef.getConnection().router);
	}

	private void configureRouter(Router router) {
		Route route = router.route("/*").handler(BodyHandler.create());

		if (method != null && !method.isEmpty()) {
			if (path == null || path.isEmpty())
				path = "/";
			route = router.route(HttpMethod.valueOf(method), path).handler(this::handleRequest);
		} else {
			if (path == null || path.isEmpty())
				route = router.route().handler(this::handleRequest);
			else
				route = router.route(path).handler(this::handleRequest);
		}
		if (failureHandler != null) {
			route.failureHandler(ctx -> {
				Message msg = failureHandler.apply(ctx.failure());
				sendResponse(ctx.response(), msg);
			});
		} else {
			route.failureHandler(this::defaultHandleException);
		}
	}

	private void defaultHandleException(RoutingContext ctx) {
		ctx.response().setStatusCode(ctx.statusCode());
		if (ctx.failure() != null)
			ctx.response().end(ctx.failure().getMessage());
		else
			ctx.response().end();
	}

	private void handleRequest(RoutingContext ctx) {
		Message request = buildMessage(ctx);
		Deferred<Message, Exception> deferred = new SimpleDeferredObject<>(
				response -> sendResponse(ctx.response(), response), ex -> sendException(ctx, ex));
		publish(request, deferred);
	}

	private void sendException(RoutingContext ctx, Exception ex) {
		ctx.fail(ex);
	}

	private void sendResponse(HttpServerResponse serverResponse, Message response) {
		if (response == null || response.getPayload() == null) {
			serverResponse.end();
			return;
		}
		if (response.getPayload().getHeaders() != null) {
			String status = response.getPayload().getHeaders().getString(VertxHttpConstants.HEADER_STATUS, null);
			if (status != null)
				serverResponse.setStatusMessage(status);
			for (Entry<String, BElement> entry : response.getPayload().getHeaders().entrySet()) {
				serverResponse.headers().add(entry.getKey(), entry.getValue().toString());
			}
		}
		if (response.getPayload().getBody() != null)
			serverResponse.end(serialize(response.getPayload().getBody()));
		else
			serverResponse.end();
	}

	private BElement deserialize(String bodyAsString) {
		if (bodyAsString == null)
			return null;
		if (format == null || format.equals("json"))
			return BElement.fromJson(bodyAsString);
		if (format.equals("xml"))
			return BElement.fromXml(bodyAsString);
		throw new UnsupportedFormatException(format);
	}

	private String serialize(BElement body) {
		if (body == null)
			return null;
		if (format == null || format.equals("json"))
			return body.toJson();
		if (format.equals("xml"))
			return body.toXml();
		throw new UnsupportedFormatException(format);
	}

	private Message buildMessage(RoutingContext ctx) {
		BObject headers = BFactory.DEFAULT.newObject();
		for (Entry<String, String> entry : ctx.request().headers()) {
			headers.put(entry.getKey(), BValue.newDefault(entry.getValue()));
		}
		BElement body = deserialize(ctx.getBodyAsString());
		return createMessage(headers, body);
	}

	@Override
	protected void onStop() {
		if (connRef.deref() == 0) {
			try {
				connRef.getConnection().server.close();
			} finally {
				connRef.getConnection().vertx.close();
			}
		}
	}

	class ServerRouterTuple {

		private HttpServer server;

		private Router router;

		private Vertx vertx;

		public ServerRouterTuple(Vertx vertx, HttpServer server, Router router) {
			this.vertx = vertx;
			this.server = server;
			this.router = router;
		}
	}
}
