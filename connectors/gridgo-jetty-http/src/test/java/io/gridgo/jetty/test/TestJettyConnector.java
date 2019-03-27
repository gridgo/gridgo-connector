package io.gridgo.jetty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.httpcommon.HttpHeader;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.jetty.JettyConnector;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class TestJettyConnector {

    private static final String TEST_TEXT = "this is test text";

    private final String HTTP_LOCALHOST_8888 = "http://localhost:8888";
    private final String baseServerEndpoint = "jetty:" + HTTP_LOCALHOST_8888;

    private final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
        }));
    }

    private Connector createConnector(String endpoint) {
        ConnectorContext connectorContext = new DefaultConnectorContextBuilder().setCallbackInvokerStrategy(new ExecutorExecutionStrategy(executor)).build();

        Connector connector = resolver.resolve(endpoint, connectorContext);

        assertNotNull(connector);
        assertTrue(connector instanceof JettyConnector);

        assertTrue(connector.getConsumer().isPresent());
        assertTrue(connector.getProducer().isPresent());

        assertTrue(connector.getConsumer().get() instanceof JettyConsumer);
        assertTrue(connector.getProducer().get() instanceof JettyResponder);

        return connector;
    }

    @Test
    public void testPingPongGET() throws URISyntaxException, IOException, InterruptedException {
        String path = "test-path";
        String endpoint = baseServerEndpoint + "/" + path + "?session=true&gzip=true&security=true";
        Connector connector = createConnector(endpoint);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer producer = connector.getProducer().get();

            consumer.subscribe((msg) -> {
                var queryParams = msg.headers().get(HttpHeader.QUERY_PARAMS.asString());
                producer.send(Message.of(msg.getRoutingId().get(), Payload.of(queryParams)));
            });

            final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
            URI uri = new URI(HTTP_LOCALHOST_8888 + "/" + path + "?key=" + encodedText);
            HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            BElement respObj = BElement.ofJson(response.body());
            assertNotNull(respObj);
            assertTrue(respObj.isObject());
            assertEquals(TEST_TEXT, respObj.asObject().getString("key"));
        } finally {
            connector.stop();
        }
    }

    @Test
    public void testPingPongPOST() throws URISyntaxException, IOException, InterruptedException {
        String path = "test-path";
        String endpoint = baseServerEndpoint + "/" + path + "?session=true&gzip=true&security=true";
        Connector connector = createConnector(endpoint);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer producer = connector.getProducer().get();

            consumer.subscribe((msg) -> {
                var queryParams = msg.headers().get(HttpHeader.QUERY_PARAMS.asString());
                var body = msg.body();
                var response = BObject.ofEmpty().set("query", queryParams).set("body", body);
                producer.send(Message.of(msg.getRoutingId().get(), Payload.of(response)));
            });

            URI uri = new URI(HTTP_LOCALHOST_8888 + "/" + path + "?paramName=abc");
            HttpRequest request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(TEST_TEXT)).uri(uri).build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            BElement respObj = BElement.ofJson(response.body());
            System.out.println(respObj);
            assertNotNull(respObj);
            assertTrue(respObj.isObject());

            assertTrue(respObj.asObject().containsKey("query"));
            assertEquals("abc", respObj.asObject().getObject("query").getString("paramName"));

            assertTrue(respObj.asObject().containsKey("body"));
            assertEquals(TEST_TEXT, respObj.asObject().getString("body"));
        } finally {
            connector.stop();
        }
    }
}
