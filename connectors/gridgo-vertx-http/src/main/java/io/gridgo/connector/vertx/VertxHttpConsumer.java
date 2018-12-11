package io.gridgo.connector.vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.httpcommon.AbstractHttpConsumer;
import io.gridgo.connector.support.ConnectionRef;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.vertx.support.exceptions.HttpException;
import io.gridgo.framework.support.Message;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpConsumer extends AbstractHttpConsumer implements Consumer {

    private static final Map<String, ConnectionRef<ServerRouterTuple>> SERVER_MAP = new HashMap<>();

    private static final int DEFAULT_EXCEPTION_STATUS_CODE = 500;

    private Vertx vertx;

    private VertxOptions vertxOptions;

    private HttpServerOptions httpOptions;

    private String path;

    private String method;

    private boolean parseCookie;

    private Route route;

    public VertxHttpConsumer(ConnectorContext context, Vertx vertx, VertxOptions vertxOptions,
            HttpServerOptions options, String path, String method, String format, Map<String, Object> params) {
        super(context, format);
        this.vertx = vertx;
        this.vertxOptions = vertxOptions;
        this.httpOptions = options;
        this.path = path;
        this.method = method;
        this.parseCookie = Boolean.valueOf(
                params.getOrDefault(VertxHttpConstants.PARAM_PARSE_COOKIE, "false").toString());
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
                Vertx vertx;
                boolean ownedVertx;
                if (this.vertx != null) {
                    vertx = this.vertx;
                    ownedVertx = false;
                } else {
                    vertx = Vertx.vertx(vertxOptions);
                    ownedVertx = true;
                }
                var server = vertx.createHttpServer(httpOptions);
                var router = initializeRouter(vertx);
                server.requestHandler(router::accept);
                connRef = new ConnectionRef<>(new ServerRouterTuple(ownedVertx ? vertx : null, server, router));
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

    private Router initializeRouter(Vertx vertx) {
        var router = Router.router(vertx);
        if (!"GET".equals(method))
            router.route("/*").handler(BodyHandler.create());
        router.route() //
              .last() //
              .handler(rc -> rc.fail(404)).failureHandler(this::handleException);
        return router;
    }

    private String buildConnectionKey() {
        return httpOptions.getHost() + ":" + httpOptions.getPort();
    }

    private void configureRouter(Router router) {
        if (method != null && !method.isEmpty()) {
            if (path == null || path.isEmpty())
                path = "/";
            this.route = router.route(HttpMethod.valueOf(method), path).handler(this::handleRequest);
        } else {
            if (path == null || path.isEmpty())
                this.route = router.route("/").handler(this::handleRequest);
            else
                this.route = router.route(path).handler(this::handleRequest);
        }
        router.route().failureHandler(this::handleException);
    }

    private void handleException(RoutingContext ctx) {
        log.error("Exception caught when handling request", ctx.failure());
        var ex = ctx.failure() != null ? ctx.failure() : new HttpException(ctx.statusCode());
        var msg = buildFailureMessage(ex);
        if (msg != null) {
            msg.getPayload().getHeaders().putIfAbsent(VertxHttpConstants.HEADER_STATUS_CODE,
                    BValue.of(DEFAULT_EXCEPTION_STATUS_CODE));
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
            ctx.response().end(ctx.failure().getMessage() + "");
        else
            ctx.response().end();
    }

    private void handleRequest(RoutingContext ctx) {
        var request = buildMessage(ctx);
        var deferred = new AsyncDeferredObject<Message, Exception>();
        publish(request, deferred);
        deferred.promise() //
                .done(response -> sendResponse(ctx.response(), response)) //
                .fail(ex -> sendException(ctx, ex));
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
            if (entry.getValue().isValue())
                serverResponse.headers().add(entry.getKey(), entry.getValue().toString());
        }
        if (response.getPayload().getBody() == null) {
            serverResponse.end();
            return;
        }
        var buffer = Buffer.buffer(serialize(response.getPayload().getBody()));
        serverResponse.end(buffer);
    }

    private Message buildMessage(RoutingContext ctx) {
        var headers = BObject.ofEmpty();
        for (var entry : ctx.request().headers()) {
            headers.put(entry.getKey(), BValue.of(entry.getValue()));
        }

        populateCommonHeaders(ctx, headers);

        if (ctx.request().method() == HttpMethod.GET)
            return createMessage(headers, null);
        var body = deserialize(ctx.getBody().getBytes());
        return createMessage(headers, body);
    }

    private void populateCommonHeaders(RoutingContext ctx, BObject headers) {
        var queryParams = BObject.ofEmpty();
        for (var query : ctx.request().params()) {
            queryParams.put(query.getKey(), BValue.of(query.getValue()));
        }
        headers.set(VertxHttpConstants.HEADER_QUERY_PARAMS, queryParams)
               .setAny(VertxHttpConstants.HEADER_HTTP_METHOD, ctx.request().method().name())
               .setAny(VertxHttpConstants.HEADER_PATH, ctx.request().path());

        if (parseCookie) {
            var cookies = BArray.ofEmpty();
            for (var cookie : ctx.cookies()) {
                var cookieObj = BObject.ofEmpty() //
                                       .setAny(VertxHttpConstants.COOKIE_NAME, cookie.getName())
                                       .setAny(VertxHttpConstants.COOKIE_DOMAIN, cookie.getDomain())
                                       .setAny(VertxHttpConstants.COOKIE_PATH, cookie.getPath())
                                       .setAny(VertxHttpConstants.COOKIE_VALUE, cookie.getValue());
                cookies.add(cookieObj);
            }
            headers.put(VertxHttpConstants.HEADER_COOKIE, cookies);
        }
    }

    @Override
    protected void onStop() {
        String connectionKey = buildConnectionKey();
        synchronized (SERVER_MAP) {
            this.route.remove();
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
    protected String generateName() {
        return "consumer.vertx:http." + method + "." + path;
    }
}
