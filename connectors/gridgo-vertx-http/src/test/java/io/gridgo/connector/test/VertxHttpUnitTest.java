package io.gridgo.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;

public class VertxHttpUnitTest {

	@Test
	public void testSimple() throws ClientProtocolException, IOException {
		Connector connector = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8080/?method=POST");
		connector.start();
		Consumer consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://localhost:8080";
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("{'abc':'def'}"));
		HttpResponse response = client.execute(request);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		Assert.assertEquals("{\"abc\":\"def\"}", result.toString());

		rd.close();

		client.close();

		connector.stop();
	}

	@Test
	public void testXml() throws ClientProtocolException, IOException {
		Connector connector = new DefaultConnectorFactory()
				.createConnector("vertx:http://127.0.0.1:8081/?method=POST&format=xml");
		connector.start();
		Consumer consumer = connector.getConsumer().orElseThrow();
		consumer.subscribe((msg, deferred) -> deferred.resolve(msg));

		String url = "http://localhost:8081";
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		request.addHeader("test-header", "XYZ");
		request.setEntity(new StringEntity("<object><string name=\"abc\" value=\"def\"/></object>"));
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("XYZ", response.getFirstHeader("test-header").getValue());
		Assert.assertEquals("<object><string name=\"abc\" value=\"def\"/></object>", result.toString());

		rd.close();

		client.close();

		connector.stop();
	}
}
