package io.gridgo.connector.httpjdk;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

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

	public HttpJdkProducer(ConnectorContext context, String endpointUri, String format, String defaultMethod) {
		super(context, format);
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
		this.httpClient.sendAsync(request, BodyHandlers.ofString()) //
				.whenComplete((response, ex) -> {
					if (ex != null)
						deferred.reject(new ConnectionException(ex));
					else
						deferred.resolve(null);
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
						deferred.reject(new ConnectionException(ex));
					else
						deferred.resolve(buildMessage(response));
				});
		return deferred.promise();
	}

	private Message buildMessage(HttpResponse<byte[]> response) {
		var headers = buildHeaders(response.headers());
		var body = deserialize(response.body());
		return createMessage(headers, body);
	}

	private BObject buildHeaders(HttpHeaders headers) {
		if (headers == null)
			return BObject.newDefault();
		var map = headers.map();
		if (map == null)
			return BObject.newDefault();
		return BObject.newDefault(map);
	}

	private HttpRequest buildRequest(Message message) {
		var endpoint = endpointUri;
		var bodyPublisher = BodyPublishers.noBody();
		var method = defaultMethod;

		if (message != null && message.getPayload() != null) {
			var body = message.getPayload().getBody();
			bodyPublisher = body != null ? BodyPublishers.ofByteArray(serialize(body)) : BodyPublishers.noBody();
			endpoint = endpointUri + parseParams(getQueryParams(message));
			method = getMethod(message, defaultMethod);
		}

		return HttpRequest.newBuilder() //
				.uri(URI.create(endpoint)) //
				.method(method, bodyPublisher) //
				.build();
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
		this.httpClient = HttpClient.newHttpClient();
	}

	@Override
	protected void onStop() {
	}

	@Override
	protected String generateName() {
		return "producer.httpjdk." + endpointUri;
	}
}
