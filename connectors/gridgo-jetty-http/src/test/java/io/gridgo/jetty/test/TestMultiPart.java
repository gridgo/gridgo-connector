package io.gridgo.jetty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.jetty.JettyConnector;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.support.HttpHeader;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class TestMultiPart {

	private static final String TEST_TEXT = "this is test text";

	private final String HTTP_LOCALHOST_8888 = "http://localhost:8888";
	private final String baseServerEndpoint = "jetty:" + HTTP_LOCALHOST_8888;

	private final HttpClient httpClient = HttpClientBuilder.create().build();

	private final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");
	private final ExecutorService executor = Executors.newCachedThreadPool();

	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			executor.shutdown();
		}));
	}

	private Connector createConnector(String endpoint) {
		ConnectorContext connectorContext = new DefaultConnectorContextBuilder() //
				.setCallbackInvokerStrategy(new ExecutorExecutionStrategy(executor)) //
				.setExceptionHandler((ex) -> {
					ex.printStackTrace();
				}) //
				.build();

		Connector connector = resolver.resolve(endpoint, connectorContext);

		assertNotNull(connector);
		assertTrue(connector instanceof JettyConnector);

		assertTrue(connector.getConsumer().isPresent());
		assertTrue(connector.getProducer().isPresent());

		assertTrue(connector.getConsumer().get() instanceof JettyConsumer);
		assertTrue(connector.getProducer().get() instanceof JettyResponder);

		return connector;
	}

	protected String readInputStreamAsString(InputStream inputStream) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result.toString(Charset.defaultCharset());
	}

	@Test
	public void testPingPongMultiPart() throws URISyntaxException, IOException, InterruptedException {
		String path = "test-path";
		String endpoint = baseServerEndpoint + "/" + path + "?session=true&gzip=true&security=true";
		Connector connector = createConnector(endpoint);
		connector.start();

		Consumer consumer = connector.getConsumer().get();
		Producer producer = connector.getProducer().get();

		consumer.subscribe((msg) -> {
			BArray arr = msg.getPayload().getBody().asObject().getArray("body");

			BObject responseBody = BObject.newDefault();
			for (BElement ele : arr) {
				String key = ele.asObject().getString("name");
				String value = null;
				if (ele.asObject().getBoolean("isConverted")) {
					value = ele.asObject().getString("value");
				} else {
					value = readInputStreamAsString(
							(InputStream) ele.asObject().getReference("inputStream").getReference());
				}
				responseBody.putAny(key, value);
			}

			BObject response = BObject.newDefault();
			response.put("query", msg.getPayload().getHeaders().get(HttpHeader.QUERY_PARAMS.asString()));
			response.put("body", responseBody);

			producer.send(Message.newDefault(msg.getRoutingId().get(), Payload.newDefault(response)));
		});

		byte[] rawData = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		InputStream testTXT = getClass().getClassLoader().getResourceAsStream("test.txt");

		HttpEntity entity = MultipartEntityBuilder.create() //
				.addBinaryBody("file:test.txt", testTXT) //
				.addBinaryBody("rawData", rawData) //
				.addTextBody("testText", TEST_TEXT) //
				.build();

		URI uri = URI.create(HTTP_LOCALHOST_8888 + "/" + path + "?param=abc");
		HttpUriRequest request = RequestBuilder.post(uri).setEntity(entity).build();

		HttpResponse response = httpClient.execute(request);

		BElement respObj = BElement.fromJson(EntityUtils.toString(response.getEntity()));

		System.out.println("Got response: " + respObj);

		assertNotNull(respObj);
		assertTrue(respObj.isObject());

		assertTrue(respObj.asObject().containsKey("query"));
		assertEquals(respObj.asObject().getObject("query").getString("param"), "abc");

		String fileContent = readInputStreamAsString(getClass().getClassLoader().getResourceAsStream("test.txt"));

		assertTrue(respObj.asObject().containsKey("body"));
		assertTrue(respObj.asObject().get("body").isObject());

		BObject body = respObj.asObject().getObject("body");
		assertEquals(TEST_TEXT, body.getString("testText"));
		assertEquals(fileContent, body.getString("file:test.txt"));
		assertEquals(new String(rawData), body.getString("rawData"));

		connector.stop();
	}
}
