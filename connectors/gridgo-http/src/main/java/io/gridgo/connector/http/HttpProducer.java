package io.gridgo.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BObject;
import io.gridgo.connector.httpcommon.AbstractHttpProducer;
import io.gridgo.connector.httpcommon.support.exceptions.ConnectionException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.resolver.NameResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProducer extends AbstractHttpProducer {

    private static final String DEFAULT_METHOD = "GET";

    private String endpointUri;

    private AsyncHttpClient asyncHttpClient;

    private Builder config;

    private NameResolver<InetAddress> nameResolver;

    private String defaultMethod;

    public HttpProducer(ConnectorContext context, String endpointUri, Builder config, String format, NameResolver<InetAddress> nameResolver,
            String defaultMethod) {
        super(context, format);
        this.endpointUri = endpointUri;
        this.config = config;
        this.nameResolver = nameResolver;
        this.defaultMethod = defaultMethod != null ? defaultMethod : DEFAULT_METHOD;
    }

    private BObject buildHeaders(HttpHeaders headers) {
        var obj = BObject.ofEmpty();
        if (headers == null)
            return obj;
        var entries = headers.entries();
        if (entries == null)
            return obj;
        entries.forEach(e -> obj.putAny(e.getKey(), e.getValue()));
        return obj;
    }

    private Message buildMessage(Response response) {
        var headers = buildHeaders(response.getHeaders()) //
                                                         .setAny(HttpConstants.HEADER_STATUS, response.getStatusText())
                                                         .setAny(HttpConstants.HEADER_STATUS_CODE, response.getStatusCode());
        var body = deserialize(response.getResponseBodyAsBytes());
        return createMessage(headers, body);
    }

    private List<Param> buildParams(BObject object) {
        return object.entrySet().stream() //
                     .filter(e -> e.getValue().isValue()) //
                     .map(e -> new Param(e.getKey(), e.getValue().asValue().getString())) //
                     .collect(Collectors.toList());
    }

    private Request buildRequest(Message message) {
        var builder = createBuilder(message);
        if (nameResolver != null)
            builder.setNameResolver(nameResolver);
        return builder.build();
    }

    @Override
    public Promise<Message, Exception> call(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        var request = buildRequest(message);
        asyncHttpClient.executeRequest(request, new AsyncCompletionHandler<Object>() {

            @Override
            public Object onCompleted(Response response) throws Exception {
                var message = buildMessage(response);
                ack(deferred, message);
                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                ack(deferred, new ConnectionException(t));
            }
        });
        return deferred.promise();
    }

    private RequestBuilder createBuilder(Message message) {
        if (message == null)
            return new RequestBuilder().setUrl(endpointUri);
        var method = getMethod(message, defaultMethod);
        var params = buildParams(getQueryParams(message));
        var body = serialize(message.body());
        return new RequestBuilder(method) //
                                         .setUrl(endpointUri) //
                                         .setBody(body) //
                                         .setQueryParams(params);

    }

    @Override
    protected String generateName() {
        return "consumer." + endpointUri;
    }

    @Override
    protected void onStart() {
        this.asyncHttpClient = Dsl.asyncHttpClient(config);
    }

    @Override
    protected void onStop() {
        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            log.error("Error when closing AsyncHttpClient", e);
        }
    }

    @Override
    public void send(Message message) {
        var request = buildRequest(message);
        asyncHttpClient.executeRequest(request);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        var request = buildRequest(message);
        asyncHttpClient.executeRequest(request, new AsyncHandler<Object>() {

            @Override
            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                ack(deferred);
                return State.CONTINUE;
            }

            @Override
            public Object onCompleted() throws Exception {
                ack(deferred);
                return State.CONTINUE;
            }

            @Override
            public State onHeadersReceived(HttpHeaders headers) throws Exception {
                ack(deferred);
                return State.CONTINUE;
            }

            @Override
            public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                ack(deferred);
                return State.CONTINUE;
            }

            @Override
            public void onThrowable(Throwable t) {
                ack(deferred, new ConnectionException(t));
            }
        });
        return deferred.promise();
    }
}
