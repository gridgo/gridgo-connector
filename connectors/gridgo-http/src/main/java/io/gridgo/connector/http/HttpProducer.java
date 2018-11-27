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
import io.gridgo.connector.http.support.ConnectionException;
import io.gridgo.connector.httpcommon.AbstractHttpProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.resolver.NameResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProducer extends AbstractHttpProducer {

	private String endpointUri;

	private AsyncHttpClient asyncHttpClient;

	private Builder config;

	private NameResolver<InetAddress> nameResolver;

	public HttpProducer(ConnectorContext context, String endpointUri, Builder config, String format,
			NameResolver<InetAddress> nameResolver) {
		super(context, format);
		this.endpointUri = endpointUri;
		this.config = config;
		this.nameResolver = nameResolver;
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
				deferred.resolve(null);
				return State.CONTINUE;
			}

			@Override
			public Object onCompleted() throws Exception {
				deferred.resolve(null);
				return State.CONTINUE;
			}

			@Override
			public State onHeadersReceived(HttpHeaders headers) throws Exception {
				deferred.resolve(null);
				return State.CONTINUE;
			}

			@Override
			public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
				deferred.resolve(null);
				return State.CONTINUE;
			}

			@Override
			public void onThrowable(Throwable t) {
				deferred.reject(new ConnectionException(t));
			}
		});
		return deferred.promise();
	}

	@Override
	public Promise<Message, Exception> call(Message message) {
		var deferred = new CompletableDeferredObject<Message, Exception>();
		var request = buildRequest(message);
		asyncHttpClient.executeRequest(request, new AsyncCompletionHandler<Object>() {

			@Override
			public void onThrowable(Throwable t) {
				deferred.reject(new ConnectionException(t));
			}

			@Override
			public Object onCompleted(Response response) throws Exception {
				var message = buildMessage(response);
				deferred.resolve(message);
				return response;
			}
		});
		return deferred.promise();
	}

	private Request buildRequest(Message message) {
		if (message == null)
			return new RequestBuilder().setUrl(endpointUri).build();
		var method = getMethod(message, "GET");
		var params = buildParams(getQueryParams(message));
		var body = serialize(message.getPayload().getBody());
		return new RequestBuilder(method) //
				.setUrl(endpointUri) //
				.setBody(body) //
				.setQueryParams(params) //
				.setNameResolver(nameResolver) //
				.build();
	}

	private List<Param> buildParams(BObject object) {
		return object.entrySet().stream() //
				.filter(e -> e.getValue().isValue()) //
				.map(e -> new Param(e.getKey(), e.getValue().asValue().getString())) //
				.collect(Collectors.toList());
	}

	private Message buildMessage(Response response) {
		var headers = buildHeaders(response.getHeaders());
		var body = deserialize(response.getResponseBody());
		return createMessage(headers, body);
	}

	private BObject buildHeaders(HttpHeaders headers) {
		var obj = BObject.newDefault();
		if (headers == null)
			return obj;
		var entries = headers.entries();
		if (entries == null)
			return obj;
		entries.forEach(e -> obj.putAny(e.getKey(), e.getValue()));
		return obj;
	}
}
