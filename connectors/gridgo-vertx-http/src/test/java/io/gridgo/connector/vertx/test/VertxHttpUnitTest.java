package io.gridgo.connector.vertx.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class VertxHttpUnitTest {

	@Test
	public void testCustomFailureHandler() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory()
				.createConnector("vertx:http://127.0.0.1:8082/?method=POST&format=xml");

		connector.start();

		Assert.assertNotNull(connector.getProducer());
		Assert.assertTrue(!connector.getProducer().isPresent());

		var consumer = connector.getConsumer().orElseThrow();
		if (consumer instanceof FailureHandlerAware) {
			((FailureHandlerAware<?>) consumer).setFailureHandler(ex -> {
				BObject headers = BObject.newDefault().setAny("error", true).setAny("cause", ex.getMessage());
				return Message.newDefault(Payload.newDefault(headers, BValue.newDefault("Error")));
			});
		}
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8082";
		var client = HttpClientBuilder.create().build();
		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(500, response.getStatusLine().getStatusCode());
		Assert.assertNull(response.getFirstHeader("test-header"));
		Assert.assertEquals("true", response.getFirstHeader("error").getValue());
		Assert.assertEquals("Cannot parse xml", response.getFirstHeader("cause").getValue());

		var result = readResponse(response);

		Assert.assertEquals("<string value=\"Error\"/>", result.toString());

		client.close();

		connector.stop();
	}

	@Test
	public void testFailure() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory()
				.createConnector("vertx:http://127.0.0.1:8082/?method=POST&format=xml");
		connector.start();
		Assert.assertNotNull(connector.getProducer());
		Assert.assertTrue(!connector.getProducer().isPresent());
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8082";
		var client = HttpClientBuilder.create().build();
		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(500, response.getStatusLine().getStatusCode());
		Assert.assertNull(response.getFirstHeader("test-header"));

		var result = readResponse(response);

		Assert.assertEquals("Cannot parse xml", result.toString());

		client.close();

		connector.stop();
	}

	@Test
	public void testSimple() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8080/?method=POST");
		connector.start();
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8080";
		var client = HttpClientBuilder.create().build();
		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());

		var result = readResponse(response);

		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());

		client.close();

		connector.stop();
	}

	@Test
	public void testAllMethods() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8080/");
		connector.start();
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8080";
		var client = HttpClientBuilder.create().build();

		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		var result = readResponse(response);
		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());
		
		var putRequest = new HttpPut(url);
		putRequest.addHeader("test-header", "XYZ");
		putRequest.setEntity(new StringEntity("{'abc':'def'}"));
		response = client.execute(putRequest);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		result = readResponse(response);
		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());

		client.close();

		connector.stop();
	}
	
	@Test
	public void testAllMethodsWithPath() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8080/api");
		connector.start();
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8080/api";
		var client = HttpClientBuilder.create().build();

		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		var result = readResponse(response);
		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());
		
		var putRequest = new HttpPut(url);
		putRequest.addHeader("test-header", "XYZ");
		putRequest.setEntity(new StringEntity("{'abc':'def'}"));
		response = client.execute(putRequest);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		result = readResponse(response);
		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());

		client.close();

		connector.stop();
	}

	@Test
	public void testCompression() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory().createConnector(
				"vertx:http://127.0.0.1:8080/?method=POST&compressionSupported=true&compressionLevel=5");
		connector.start();
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8080";
		var client = HttpClientBuilder.create().build();
		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		var response = client.execute(request);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		Assert.assertEquals("gzip,deflate", response.getFirstHeader("Accept-Encoding").getValue());

		var result = readResponse(response);

		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());

		client.close();

		connector.stop();
	}

	private StringBuffer readResponse(HttpResponse response) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result;
	}

	@Test
	public void testXml() throws ClientProtocolException, IOException {
		var connector = new DefaultConnectorFactory()
				.createConnector("vertx:http://127.0.0.1:8081/?method=POST&format=xml");
		connector.start();
		var consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://127.0.0.1:8081";
		var client = HttpClientBuilder.create().build();
		var request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("<object><string name=\"abc\" value=\"def\"/></object>"));
		var response = client.execute(request);

		var result = readResponse(response);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		Assert.assertEquals("<object><string name=\"abc\" value=\"def\"/></object>", result.toString());

		client.close();

		connector.stop();
	}
}
