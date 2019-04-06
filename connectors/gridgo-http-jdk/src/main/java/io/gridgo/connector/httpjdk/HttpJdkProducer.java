package io.gridgo.connector.httpjdk;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_HTTP_HEADERS;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_PATH;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.HEADER_STATUS_CODE;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.httpcommon.AbstractHttpProducer;
import io.gridgo.connector.httpcommon.support.exceptions.ConnectionException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;

public class HttpJdkProducer extends AbstractHttpProducer {

    private static final String DEFAULT_METHOD = "GET";

    private String endpointUri;

    private String defaultMethod;

    private HttpClient httpClient;

    private Builder builder;

    public HttpJdkProducer(ConnectorContext context, HttpClient.Builder builder, String endpointUri, String format,
            String defaultMethod) {
        super(context, format);
        this.builder = builder;
        this.endpointUri = endpointUri;
        this.defaultMethod = defaultMethod != null ? defaultMethod : DEFAULT_METHOD;
    }

    @Override
    public void send(Message message) {
        var request = buildRequest(message);
        this.httpClient.sendAsync(request, BodyHandlers.discarding());
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        var request = buildRequest(message);
        this.httpClient.sendAsync(request, BodyHandlers.discarding()) //
                       .whenComplete((response, ex) -> {
                           if (ex != null)
                               ack(deferred, new ConnectionException(ex));
                           else
                               ack(deferred);
                       });
        return deferred.promise();
    }

    @Override
    public Promise<Message, Exception> call(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        var request = buildRequest(message);
        this.httpClient.sendAsync(request, BodyHandlers.ofByteArray()) //
                       .whenComplete((response, ex) -> {
                           if (ex != null)
                               ack(deferred, new ConnectionException(ex));
                           else
                               ack(deferred, buildMessage(response));
                       });
        return deferred.promise();
    }

    private Message buildMessage(HttpResponse<byte[]> response) {
        var headers = buildHeaders(response.headers()) //
                                                      .setAny(HEADER_STATUS_CODE, response.statusCode());
        var body = deserialize(response.body());
        return createMessage(headers, body);
    }

    private BObject buildHeaders(HttpHeaders headers) {
        if (headers == null)
            return BObject.ofEmpty();
        var map = headers.map();
        if (map == null)
            return BObject.ofEmpty();
        return BObject.of(map);
    }

    private HttpRequest buildRequest(Message message) {
        var endpoint = endpointUri;
        var bodyPublisher = BodyPublishers.noBody();
        var method = defaultMethod;
        var headers = BObject.ofEmpty();

        if (message != null && message.getPayload() != null) {
            headers = message.headers();
            var body = message.body();
            bodyPublisher = body != null ? BodyPublishers.ofByteArray(serialize(body)) : BodyPublishers.noBody();
            endpoint = endpointUri + getPath(message) + parseParams(getQueryParams(message));
            method = getMethod(message, defaultMethod);
        }

        var request = HttpRequest.newBuilder() //
                                 .uri(URI.create(endpoint)) //
                                 .method(method, bodyPublisher);
        populateHeaders(headers.getObjectOrEmpty(HEADER_HTTP_HEADERS), request);
        return request.build();
    }

    private String getPath(Message message) {
        return message.headers().getString(HEADER_PATH, "");
    }

    private void populateHeaders(BObject headers, java.net.http.HttpRequest.Builder request) {
        for (var entry : headers.entrySet()) {
            if (entry.getValue().isArray()) {
                putMultiHeaders(request, entry.getKey(), entry.getValue().asArray());
            } else {
                putHeader(request, entry.getKey(), entry.getValue());
            }
        }
    }

    private void putMultiHeaders(java.net.http.HttpRequest.Builder request, String key, BArray arr) {
        for (var e : arr) {
            putHeader(request, key, e);
        }
    }

    private void putHeader(java.net.http.HttpRequest.Builder request, String key, BElement e) {
        if (e.isValue())
            request.header(key, e.asValue().getString());
    }

    private String parseParams(BObject queryParams) {
        var sb = new StringBuilder();
        var first = true;
        for (var entry : queryParams.entrySet()) {
            if (entry.getValue().isValue()) {
                if (!first)
                    sb.append("&");
                var encodedValue = URLEncoder.encode(entry.getValue().asValue().getString(), Charset.forName("utf-8"));
                sb.append(entry.getKey() + "=" + encodedValue);
                first = false;
            }
        }
        var s = sb.toString();
        return s.isEmpty() ? "" : "?" + s;
    }

    @Override
    protected void onStart() {
        this.httpClient = builder.build();
    }

    @Override
    protected void onStop() {
        // Nothing to do here
    }

    @Override
    protected String generateName() {
        return "producer.httpjdk." + endpointUri;
    }
}
