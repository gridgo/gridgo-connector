package io.gridgo.connector.vertx.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joo.promise4j.Deferred;
import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class VertxHttpUnitTest {

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

    private Deferred<Message, Exception> resolveSimple(Message request, Deferred<Message, Exception> deferred, int i) {
        return deferred.resolve(Message.of(Payload.of(BValue.of(i))));
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

    private void testApi1(CloseableHttpClient client) throws IOException, ClientProtocolException {
        var request2 = new HttpGet("http://127.0.0.1:8083/api1");
        var result2 = readResponse(client.execute(request2)).toString();
        Assert.assertEquals("2", result2);
    }

    private void testApi2(CloseableHttpClient client) throws IOException, ClientProtocolException {
        var request3 = new HttpGet("http://127.0.0.1:8083/api2");
        var result3 = readResponse(client.execute(request3)).toString();
        Assert.assertEquals("3", result3);
    }

    @Test
    public void testCompression() throws ClientProtocolException, IOException {
        var connector = new DefaultConnectorFactory().createConnector(
                "vertx:http://127.0.0.1:8080/?method=POST&gzip=true&compressionLevel=5");
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

    @Test
    public void testCustomFailureHandler() throws ClientProtocolException, IOException {
        var connectorContext = new DefaultConnectorContextBuilder().setFailureHandler(ex -> {
            BObject headers = BObject.ofEmpty().setAny("error", true).setAny("cause", ex.getMessage());
            return Message.of(Payload.of(headers, BValue.of("Error")));
        }).build();
        var connector = new DefaultConnectorFactory().createConnector(
                "vertx:http://127.0.0.1:8082/?method=POST&format=xml", connectorContext);

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
        Assert.assertEquals("true", response.getFirstHeader("error").getValue());
        Assert.assertEquals("Cannot parse xml", response.getFirstHeader("cause").getValue());

        var result = readResponse(response);

        Assert.assertEquals("<string value=\"Error\"/>", result.toString());

        client.close();

        connector.stop();
    }

    @Test
    public void testFailure() throws ClientProtocolException, IOException {
        var connector = new DefaultConnectorFactory().createConnector(
                "vertx:http://127.0.0.1:8082/?method=POST&format=xml");
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
    public void testMultiConnector() throws ClientProtocolException, IOException {
        var connector1 = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8083/");
        var connector2 = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8083/api1");
        var connector3 = new DefaultConnectorFactory().createConnector("vertx:http://127.0.0.1:8083/api2");
        connector1.start();
        connector2.start();
        connector3.start();

        var consumer1 = connector1.getConsumer().orElseThrow();
        var consumer2 = connector2.getConsumer().orElseThrow();
        var consumer3 = connector3.getConsumer().orElseThrow();

        consumer1.subscribe((request, deferred) -> resolveSimple(request, deferred, 1));
        consumer2.subscribe((request, deferred) -> resolveSimple(request, deferred, 2));
        consumer3.subscribe((request, deferred) -> resolveSimple(request, deferred, 3));

        var client = HttpClientBuilder.create().build();
        testRoot(client);
        testApi1(client);
        testApi2(client);

        connector1.stop();

        var request4 = new HttpGet("http://127.0.0.1:8083");
        var response4 = client.execute(request4);
        Assert.assertEquals(404, response4.getStatusLine().getStatusCode());

        testApi1(client);
        testApi2(client);

        connector2.stop();
        connector3.stop();
    }

    private void testRoot(CloseableHttpClient client) throws IOException, ClientProtocolException {
        var request1 = new HttpGet("http://127.0.0.1:8083");
        var result1 = readResponse(client.execute(request1)).toString();
        Assert.assertEquals("1", result1);
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
    public void testXml() throws ClientProtocolException, IOException {
        var connector = new DefaultConnectorFactory().createConnector(
                "vertx:http://127.0.0.1:8081/?method=POST&format=xml");
        connector.start();
        var consumer = connector.getConsumer().orElseThrow();
        consumer.subscribe((msg, deferred) -> {
            deferred.resolve(Message.ofAny(BObject.of("test-header", "XYZ"), msg.body()));
        });

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
