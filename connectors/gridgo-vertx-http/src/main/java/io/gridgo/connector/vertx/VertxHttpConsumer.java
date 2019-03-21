package io.gridgo.connector.vertx;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_HTTP_METHOD;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_QUERY_PARAMS;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_STATUS;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_STATUS_CODE;
import static io.gridgo.connector.httpcommon.HttpCommonConsumerConstants.HEADER_PATH;
import static io.gridgo.connector.vertx.VertxHttpConstants.COOKIE_DOMAIN;
import static io.gridgo.connector.vertx.VertxHttpConstants.COOKIE_NAME;
import static io.gridgo.connector.vertx.VertxHttpConstants.COOKIE_PATH;
import static io.gridgo.connector.vertx.VertxHttpConstants.COOKIE_VALUE;
import static io.gridgo.connector.vertx.VertxHttpConstants.HEADER_CONTENT_TYPE;
import static io.gridgo.connector.vertx.VertxHttpConstants.HEADER_COOKIE;
import static io.gridgo.connector.vertx.VertxHttpConstants.HEADER_OUTPUT_STREAM;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_PARSE_COOKIE;

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
import io.gridgo.connector.support.exceptions.NoSubscriberException;
import io.gridgo.connector.vertx.support.exceptions.HttpException;
import io.gridgo.framework.support.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private static final Map<String, ConnectionRef<ServerRouterTuple>> SERVER_MAP = new HashMap<>();

    private static final ThreadLocal<Map<String, ConnectionRef<ServerRouterTuple>>> LOCAL_SERVER_MAP //
            = ThreadLocal.withInitial(HashMap::new);

    private static final int DEFAULT_EXCEPTION_STATUS_CODE = 500;

    private Vertx vertx;

    private VertxOptions vertxOptions;

    private HttpServerOptions httpOptions;

    private String path;

    private String method;

    private boolean parseCookie;

    private Route route;

    private boolean wrap;

    public VertxHttpConsumer(ConnectorContext context, Vertx vertx, VertxOptions vertxOptions,
            HttpServerOptions options, String path, String method, String format, Map<String, Object> params) {
        super(context, format);
        this.vertx = vertx;
        this.vertxOptions = vertxOptions;
        this.httpOptions = options;
        this.path = path;
        this.method = method;
        this.wrap = "true".equals(params.get(VertxHttpConstants.WRAP_RESPONSE));
        this.parseCookie = Boolean.valueOf(params.getOrDefault(PARAM_PARSE_COOKIE, "false").toString());
    }

    private String buildConnectionKey() {
        return httpOptions.getHost() + ":" + httpOptions.getPort();
    }

    private Message buildMessage(RoutingContext ctx) {
        var headers = BObject.ofEmpty();
        for (var entry : ctx.request().headers()) {
            headers.put(entry.getKey(), BValue.of(entry.getValue()));
        }

        populateCommonHeaders(ctx, headers);

        if (ctx.request().method() == HttpMethod.GET)
            return createMessage(headers, null);
        var body = ctx.getBody() != null ? deserialize(ctx.getBody().getBytes()) : null;
        return createMessage(headers, body);
    }

    private void configureRouter(Router router) {
        var route = parseRoute(router);
        this.route = route.handler(this::handleRequest);
        router.route().failureHandler(this::handleException);
    }

    private Route parseRoute(Router router) {
        if (method != null && !method.isEmpty()) {
            if (path == null || path.isEmpty())
                path = "/";
            return router.route(HttpMethod.valueOf(method), path);
        }
        if (path == null || path.isEmpty())
            return router.route("/");
        return router.route(path);
    }

    private ConnectionRef<ServerRouterTuple> createOrGetConnection(boolean ownedVertx, String connectionKey,
            Map<String, ConnectionRef<ServerRouterTuple>> theMap) {
        ConnectionRef<ServerRouterTuple> connRef;
        if (theMap.containsKey(connectionKey)) {
            connRef = theMap.get(connectionKey);
        } else {
            connRef = createConnection(ownedVertx, connectionKey, theMap);
        }
        connRef.ref();
        return connRef;
    }

    private ConnectionRef<ServerRouterTuple> createConnection(boolean ownedVertx, String connectionKey,
            Map<String, ConnectionRef<ServerRouterTuple>> theMap) {
        ConnectionRef<ServerRouterTuple> connRef;
        var latch = new CountDownLatch(1);
        var theVertx = this.vertx;
        if (theVertx == null) {
            theVertx = Vertx.vertx(vertxOptions);
        }
        var server = theVertx.createHttpServer(httpOptions);
        var router = initializeRouter(theVertx);
        server.requestHandler(router::accept);
        connRef = new ConnectionRef<>(new ServerRouterTuple(ownedVertx ? theVertx : null, server, router));
        theMap.put(connectionKey, connRef);
        server.listen(result -> latch.countDown());
        if (ownedVertx) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        return connRef;
    }

    private void defaultHandleException(RoutingContext ctx) {
        var statusCode = ctx.statusCode() != -1 ? ctx.statusCode() : DEFAULT_EXCEPTION_STATUS_CODE;
        ctx.response().setStatusCode(statusCode);

        if (ctx.failure() != null && ctx.failure().getMessage() != null)
            ctx.response().end(ctx.failure().getMessage());
        else
            ctx.response().end();
    }

    @Override
    protected String generateName() {
        return "consumer.vertx:http." + method + "." + path;
    }

    private String getContentType() {
        var format = getFormat();
        if ("raw".equals(format))
            return "application/octet-stream; charset=utf-8";
        if (format == null || "json".equals(format))
            return "application/json; charset=utf-8";
        if ("xml".equals(format))
            return "application/xml; charset=utf-8";
        return "text/plain; charset=utf-8";
    }

    private void handleException(RoutingContext ctx) {
        var ex = ctx.failure() != null ? ctx.failure() : new HttpException(ctx.statusCode());
        if (ex instanceof HttpException)
            log.info("HTTP error {} when handling request {}", ((HttpException) ex).getCode(), ctx.request().path());
        else
            log.error("Exception caught when handling request", ex);
        var msg = buildFailureMessage(ex);
        if (msg == null) {
            defaultHandleException(ctx);
            return;
        }
        var statusCode = ctx.statusCode() != -1 ? ctx.statusCode() : DEFAULT_EXCEPTION_STATUS_CODE;
        msg.headers().putIfAbsent(HEADER_STATUS_CODE, BValue.of(statusCode));
        sendResponse(ctx, msg, true);
    }

    private void handleRequest(RoutingContext ctx) {
        if (getSubscribers().isEmpty()) {
            sendException(ctx, new NoSubscriberException());
            return;
        }
        var request = buildMessage(ctx);
        var deferred = new AsyncDeferredObject<Message, Exception>();
        publish(request, deferred);
        deferred.promise() //
                .done(response -> sendResponse(ctx, response, false)) //
                .fail(ex -> sendException(ctx, ex));
    }

    private Router initializeRouter(Vertx vertx) {
        var router = Router.router(vertx);
        router.route("/*").handler(BodyHandler.create());
        router.route() //
              .last() //
              .handler(rc -> rc.fail(404)) //
              .failureHandler(this::handleException);
        return router;
    }

    @Override
    protected void onStart() {
        var ownedVertx = this.vertx == null;
        ConnectionRef<ServerRouterTuple> connRef;
        String connectionKey = buildConnectionKey();
        if (ownedVertx) {
            synchronized (SERVER_MAP) {
                connRef = createOrGetConnection(true, connectionKey, SERVER_MAP);
            }
        } else {
            connRef = createOrGetConnection(false, connectionKey, LOCAL_SERVER_MAP.get());
        }

        configureRouter(connRef.getConnection().router);
    }

    @Override
    protected void onStop() {
        this.route.remove();

        if (this.vertx != null)
            return;

        String connectionKey = buildConnectionKey();
        synchronized (SERVER_MAP) {
            if (SERVER_MAP.containsKey(connectionKey)) {
                removeConnection(connectionKey);
            }
        }
    }

    private void removeConnection(String connectionKey) {
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

    private void populateCommonHeaders(RoutingContext ctx, BObject headers) {
        var queryParams = BObject.ofEmpty();
        for (var query : ctx.request().params()) {
            queryParams.put(query.getKey(), BValue.of(query.getValue()));
        }
        headers.set(HEADER_QUERY_PARAMS, queryParams).setAny(HEADER_HTTP_METHOD, ctx.request().method().name())
               .setAny(HEADER_PATH, ctx.request().path());

        if (parseCookie) {
            var cookies = BArray.ofEmpty();
            for (var cookie : ctx.cookies()) {
                var cookieObj = BObject.ofEmpty() //
                                       .setAny(COOKIE_NAME, cookie.getName()).setAny(COOKIE_DOMAIN, cookie.getDomain())
                                       .setAny(COOKIE_PATH, cookie.getPath()).setAny(COOKIE_VALUE, cookie.getValue());
                cookies.add(cookieObj);
            }
            headers.put(HEADER_COOKIE, cookies);
        }

        headers.setAny(HEADER_OUTPUT_STREAM, ctx.response());
    }

    private void sendException(RoutingContext ctx, Exception ex) {
        ctx.fail(ex);
    }

    private void sendResponse(RoutingContext ctx, Message response, boolean fromException) {
        var serverResponse = ctx.response();
        if (response == null || response.getPayload() == null) {
            serverResponse.end();
            return;
        }

        var status = response.headers().getString(HEADER_STATUS, null);
        if (status != null)
            serverResponse.setStatusMessage(status);
        int statusCode = response.headers().getInteger(HEADER_STATUS_CODE, -1);
        if (statusCode != -1)
            serverResponse.setStatusCode(statusCode);

        var headers = response.headers();

        if (!headers.containsKey(HEADER_CONTENT_TYPE)) {
            headers.setAny(HEADER_CONTENT_TYPE, getContentType());
        }

        for (var entry : headers.entrySet()) {
            if (entry.getValue().isValue())
                serverResponse.headers().add(entry.getKey(), entry.getValue().asValue().getString());
        }
        var body = response.body();
        if (body == null || body.isNullValue()) {
            serverResponse.end();
            return;
        }

        if (body.isReference()) {
            handleReferenceResponse(serverResponse, body.asReference().getReference());
            return;
        }

        byte[] bytes;
        try {
            bytes = serialize(body);
        } catch (Exception ex) {
            log.error("Exception caught while sending response", ex);
            if (!fromException)
                ctx.fail(ex);
            return;
        }
        if (wrap) {
            serverResponse.end(Buffer.buffer(Unpooled.wrappedBuffer(bytes)));
        } else {
            serverResponse.end(Buffer.buffer(bytes));
        }
    }

    private void handleReferenceResponse(HttpServerResponse response, Object ref) {
        if (ref instanceof ByteBuf) {
            response.end(Buffer.buffer((ByteBuf) ref));
        } else if (ref instanceof Buffer) {
            response.end((Buffer) ref);
        } else {
            throw new IllegalArgumentException("Response of type BReference must be either Buffer or ByteBuf");
        }
    }
}
