package io.gridgo.jetty.test;

import static io.gridgo.connector.jetty.support.HttpEntityHelper.parseAsMultiPart;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.httpcommon.HttpContentType;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.jetty.JettyConnector;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.support.HttpEntityHelper;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class TestJettyResponseContentType {

    private static final String TEST_TEXT = "this is test text";

    private static final String URI = "http://localhost:8888/";
    private static final String SERVER_ENDPOINT = "jetty:" + URI;

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

    @Test
    public void testResponseBinary() throws URISyntaxException, IOException, InterruptedException {

        System.out.println("Test response content type application/octet-stream");

        Connector connector = createConnector(SERVER_ENDPOINT);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer responder = connector.getProducer().get();

            consumer.subscribe(msg -> {
                Payload payload = Payload.of(BReference.of(getClass().getClassLoader().getResourceAsStream("test.txt")));

                payload.addHeader(HttpCommonConstants.CONTENT_TYPE, HttpContentType.APPLICATION_OCTET_STREAM.getMime());
                Message message = Message.of(payload).setRoutingId(msg.getRoutingId().get());
                responder.send(message);
            });

            var request = RequestBuilder.get(URI).build();
            var response = httpClient.execute(request);

            String contentType = response.getEntity().getContentType().getValue();
            assertThat(contentType, Matchers.startsWith(HttpContentType.APPLICATION_OCTET_STREAM.getMime()));

            String content = HttpEntityHelper.parseAsString(response.getEntity().getContent());
            String fileContent = HttpEntityHelper.parseAsString(getClass().getClassLoader().getResourceAsStream("test.txt"));

            assertEquals(fileContent, content);
        } finally {
            connector.stop();
        }
    }

    @Test
    public void testResponseJson() throws URISyntaxException, IOException, InterruptedException {

        System.out.println("Test response content type application/json");

        Connector connector = createConnector(SERVER_ENDPOINT);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer responder = connector.getProducer().get();

            consumer.subscribe(msg -> {
                Payload payload = Payload.of(BObject.ofSequence("testText", TEST_TEXT));
                payload.addHeader(HttpCommonConstants.CONTENT_TYPE, HttpContentType.APPLICATION_JSON.getMime());
                Message message = Message.of(payload).setRoutingId(msg.getRoutingId().get());
                responder.send(message);
            });

            var request = RequestBuilder.get(URI).build();
            var response = httpClient.execute(request);
            Header[] contentType = response.getHeaders(HttpCommonConstants.CONTENT_TYPE);
            assertNotNull(contentType);
            assertTrue(contentType.length > 0);
            assertThat(contentType[0].getValue(), Matchers.startsWith(HttpContentType.APPLICATION_JSON.getMime()));

            BElement responseData = BElement.ofJson(EntityUtils.toString(response.getEntity()));
            assertNotNull(responseData);
            assertTrue(responseData.isObject());
            assertEquals(TEST_TEXT, responseData.asObject().getString("testText"));
        } finally {
            connector.stop();
        }
    }

    @Test
    public void testResponseMultiPart() throws URISyntaxException, IOException, InterruptedException {

        System.out.println("Test response content type multipart/form-data");

        Connector connector = createConnector(SERVER_ENDPOINT);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer responder = connector.getProducer().get();

            consumer.subscribe(msg -> {
                Payload payload = Payload.of( //
                        BObject.ofEmpty() //
                               .setAny("testText", TEST_TEXT) //
                               .setAny("testFile", getClass().getClassLoader().getResourceAsStream("test.txt")) //
                               .setAny("testJsonObject", BObject.ofSequence("keyString", "valueString", "keyNumber", 100)) //
                               .setAny("testJsonArray", BArray.ofSequence("string", 100, true)) //
                );

                payload.addHeader(HttpCommonConstants.CONTENT_TYPE, HttpContentType.MULTIPART_FORM_DATA.getMime());
                Message message = Message.of(payload).setRoutingId(msg.getRoutingId().get());
                responder.send(message);
            });

            var request = RequestBuilder.get(URI).build();
            var response = httpClient.execute(request);

            String contentType = response.getEntity().getContentType().getValue();
            assertThat(contentType, Matchers.startsWith(HttpContentType.MULTIPART_FORM_DATA.getMime()));

            BArray responseMultiPart = parseAsMultiPart(response.getEntity());
            assertEquals(4, responseMultiPart.size());

        } finally {
            connector.stop();
        }
    }

    @Test
    public void testResponseTextPlain() throws URISyntaxException, IOException, InterruptedException {

        System.out.println("Test response content type text/plain");

        Connector connector = createConnector(SERVER_ENDPOINT);
        connector.start();

        try {
            Consumer consumer = connector.getConsumer().get();
            Producer responder = connector.getProducer().get();

            consumer.subscribe(msg -> {
                Payload payload = Payload.of(BValue.of(TEST_TEXT));
                payload.addHeader(HttpCommonConstants.CONTENT_TYPE, HttpContentType.TEXT_PLAIN.getMime());
                Message message = Message.of(payload).setRoutingId(msg.getRoutingId().get());
                responder.send(message);
            });
            var request = RequestBuilder.get(URI).build();
            var response = httpClient.execute(request);
            Header[] contentType = response.getHeaders(HttpCommonConstants.CONTENT_TYPE);
            assertNotNull(contentType);
            assertTrue(contentType.length > 0);
            assertThat(contentType[0].getValue(), Matchers.startsWith(HttpContentType.TEXT_PLAIN.getMime()));

            BElement responseData = BElement.ofJson(EntityUtils.toString(response.getEntity()));
            assertNotNull(responseData);
            assertTrue(responseData.isValue());
            assertEquals(TEST_TEXT, responseData.asValue().getString());
        } finally {
            connector.stop();
        }
    }
}
