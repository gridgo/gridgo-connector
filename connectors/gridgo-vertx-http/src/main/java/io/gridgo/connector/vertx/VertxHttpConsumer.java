package io.gridgo.connector.vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.joo.promise4j.impl.SimpleDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.ConnectionRef;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.connector.vertx.support.exceptions.UnsupportedFormatException;
import io.gridgo.framework.support.Message;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class VertxHttpConsumer extends AbstractConsumer implements Consumer, FailureHandlerAware<VertxHttpConsumer> {

	private static final Map<String, ConnectionRef<ServerRouterTuple>> SERVER_MAP = new HashMap<>();

	private static final int DEFAULT_EXCEPTION_STATUS_CODE = 500;

	private VertxOptions vertxOptions;

	private HttpServerOptions httpOptions;

	private String path;

	private String method;

	private String format;

	private Function<Throwable, Message> failureHandler;

	public VertxHttpConsumer(ConnectorContext context, VertxOptions vertxOptions, HttpServerOptions options,
			String path, String method, String format) {
		super(context);
		this.vertxOptions = vertxOptions;
		this.httpOptions = options;
		this.path = path;
		this.method = method;
		this.format = format;
	}

	@Override
	protected void onStart() {
		ConnectionRef<ServerRouterTuple> connRef;
		String connectionKey = buildConnectionKey();
		synchronized (SERVER_MAP) {
			if (SERVER_MAP.containsKey(connectionKey)) {
				connRef = SERVER_MAP.get(connectionKey);
			} else {
				var latch = new CountDownLatch(1);
				var vertx = Vertx.vertx(vertxOptions);
				var server = vertx.createHttpServer(httpOptions);
				var router = Router.router(vertx);
				server.requestHandler(router::accept);
				connRef = new ConnectionRef<>(new ServerRouterTuple(vertx, server, router));
				SERVER_MAP.put(connectionKey, connRef);
				server.listen(result -> latch.countDown());
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			connRef.ref();
		}

		configureRouter(connRef.getConnection().router);
	}

	private String buildConnectionKey() {
		return httpOptions.getHost() + ":" + httpOptions.getPort();
	}

	private void configureRouter(Router router) {
		var route = router.route("/*").handler(BodyHandler.create());

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
		route.failureHandler(this::handleException);
	}

	private void handleException(RoutingContext ctx) {
		if (failureHandler != null) {
			var msg = failureHandler.apply(ctx.failure());
			msg.getPayload().getHeaders().putIfAbsent(VertxHttpConstants.HEADER_STATUS_CODE,
					BValue.newDefault(DEFAULT_EXCEPTION_STATUS_CODE));
			sendResponse(ctx.response(), msg);
		} else {
			defaultHandleException(ctx);
		}
	}

	private void defaultHandleException(RoutingContext ctx) {
		if (ctx.statusCode() != -1)
			ctx.response().setStatusCode(ctx.statusCode());
		else
			ctx.response().setStatusCode(DEFAULT_EXCEPTION_STATUS_CODE);

		if (ctx.failure() != null)
			ctx.response().end(ctx.failure().getMessage());
		else
			ctx.response().end();
	}

	private void handleRequest(RoutingContext ctx) {
		var request = buildMessage(ctx);
		var deferred = new SimpleDeferredObject<Message, Exception>(response -> sendResponse(ctx.response(), response),
				ex -> sendException(ctx, ex));
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
		String status = response.getPayload().getHeaders().getString(VertxHttpConstants.HEADER_STATUS, null);
		if (status != null)
			serverResponse.setStatusMessage(status);
		int statusCode = response.getPayload().getHeaders().getInteger(VertxHttpConstants.HEADER_STATUS_CODE, -1);
		if (statusCode != -1)
			serverResponse.setStatusCode(statusCode);

		for (var entry : response.getPayload().getHeaders().entrySet()) {
			serverResponse.headers().add(entry.getKey(), entry.getValue().toString());
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
		var headers = BObject.newDefault();
		for (var entry : ctx.request().headers()) {
			headers.put(entry.getKey(), BValue.newDefault(entry.getValue()));
		}
		var body = deserialize(ctx.getBodyAsString());
		return createMessage(headers, body);
	}

	@Override
	protected void onStop() {
		String connectionKey = buildConnectionKey();
		synchronized (SERVER_MAP) {
			if (SERVER_MAP.containsKey(connectionKey)) {
				var connRef = SERVER_MAP.get(connectionKey);
				if (connRef.deref() == 0) {
					SERVER_MAP.remove(connectionKey);
					try {
						connRef.getConnection().server.close();
					} finally {
						connRef.getConnection().vertx.close();
					}
				}
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

	@Override
	public VertxHttpConsumer setFailureHandler(Function<Throwable, Message> failureHandler) {
		this.failureHandler = failureHandler;
		return this;
	}
}
