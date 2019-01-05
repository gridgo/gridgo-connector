package io.gridgo.connector.netty4.test.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.PromiseException;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.netty4.Netty4Connector;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.NonNull;

public abstract class AbstractNetty4UnitTest {

    private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private final static String TEXT = "this is test text";

    private void assertNetty4Connector(Connector connector) {
        assertNotNull(connector);
        assertTrue(connector instanceof Netty4Connector);
        assertTrue(connector.getProducer().isPresent());
        assertTrue(connector.getConsumer().isPresent());
    }

    protected void testCloseServer(@NonNull String transport, String path)
            throws InterruptedException, PromiseException {

        System.out.println("****** Netty4 " + transport + " - test shutdown server while client still alive");
        final String host = "localhost:8889";

        Connector serverConnector = RESOLVER.resolve(
                "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(serverConnector);

        Connector clientConnector = RESOLVER.resolve(
                "netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(clientConnector);

        // server side
        Consumer serverConsumer = serverConnector.getConsumer().get();

        // client side
        Producer clientProducer = clientConnector.getProducer().get();
        Consumer clientReceiver = clientConnector.getConsumer().get();

        final CountDownLatch doneSignal1 = new CountDownLatch(1);

        System.out.println("Subscribe to server consumer");
        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            BValue routingId = msg.getRoutingId().orElse(BValue.of(-1));
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " server] - socket open, routing id: " + routingId);
                break;
            case "close":
                System.out.println("[" + transport + " server] - socket closed, routing id: " + routingId);
                break;
            case "message":
                System.out.println("[" + transport + " server] - got message from routing id " + routingId + ": "
                        + msg.getPayload().toBArray()
                        + " --> trigger done to close server while client still alive...");
                doneSignal1.countDown();
                break;
            }
        });

        ((FailureHandlerAware<?>) clientReceiver).setFailureHandler((cause) -> {
            System.err.println("[" + transport + " client] - exception...");
            cause.printStackTrace();
        });

        serverConnector.start();
        clientConnector.start();

        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        assertTrue(serverConsumer instanceof FailureHandlerAware<?>);
        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            System.err.println("[" + transport + " server] - exception...");
            cause.printStackTrace();
            exceptionRef.set(cause);
            doneSignal1.countDown();
        });

        clientProducer.sendWithAck(Message.of(Payload.of(BValue.of(TEXT)))).get();

        doneSignal1.await();

        // add listener to client receiver
        final CountDownLatch doneSignal2 = new CountDownLatch(1);
        clientReceiver.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "close":
                System.out.println("[" + transport + " client] - connection closed");
                doneSignal2.countDown();
                break;
            case "open":
                System.out.println("[" + transport + " client] - connection established");
                break;
            }
        });

        serverConnector.stop();
        doneSignal2.await();

        clientConnector.stop();
    }

    protected void testCloseSocketFromClient(@NonNull String transport, String path)
            throws InterruptedException, PromiseException {

        System.out.println("****** Netty4 " + transport + " - test close connection from client");
        final String host = "localhost:8889";

        Connector serverConnector = RESOLVER.resolve(
                "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(serverConnector);

        Connector clientConnector = RESOLVER.resolve(
                "netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(clientConnector);

        // server side
        Consumer serverConsumer = serverConnector.getConsumer().get();

        // client side
        Producer clientProducer = clientConnector.getProducer().get();
        Consumer clientReceiver = clientConnector.getConsumer().get();

        final CountDownLatch doneSignal = new CountDownLatch(2);

        System.out.println("Subscribe to server consumer");
        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            BValue routingId = msg.getRoutingId().orElse(BValue.of(-1));
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " server] - socket open, routing id: " + routingId);
                break;
            case "close":
                System.out.println("[" + transport + " server] - socket closed, routing id: " + routingId);
                doneSignal.countDown();
                break;
            case "message":
                System.out.println("[" + transport + " server] - got message from routing id " + routingId + ": "
                        + msg.getPayload().toBArray());
                break;
            }
        });

        clientReceiver.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "close":
                System.out.println("[" + transport + " client] - connection closed");
                doneSignal.countDown();
                break;
            case "open":
                System.out.println("[" + transport + " client] - connection established");
                break;
            }
        });

        ((FailureHandlerAware<?>) clientReceiver).setFailureHandler((cause) -> {
            System.err.println("[" + transport + " client] - exception...");
            cause.printStackTrace();
        });

        serverConnector.start();
        clientConnector.start();

        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        assertTrue(serverConsumer instanceof FailureHandlerAware<?>);
        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            if ("connection reset by peer".equalsIgnoreCase(cause.getMessage())
                    || "An existing connection was forcibly closed by the remote host".equals(cause.getMessage())) {
                return;
            }
            cause.printStackTrace();
            exceptionRef.set(cause);
            doneSignal.countDown();
        });

        clientProducer.sendWithAck(Message.of(Payload.of(BValue.of(TEXT)))).get();

        System.out.println("[" + transport + " client] - close connection by stop client producer");
        clientProducer.stop();

        doneSignal.await();

        // the exception should be: connection reset by peer
        assertNull(exceptionRef.get());

        System.out.println("Close server connector");
        serverConnector.stop();

        System.out.println("Close client connector");
        clientConnector.stop();
    }

    protected void testCloseSocketFromServer(@NonNull String transport, String path)
            throws InterruptedException, PromiseException {

        System.out.println("****** Netty4 " + transport + " - test close connection from server");
        final String host = "localhost:8889";

        Connector serverConnector = RESOLVER.resolve(
                "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(serverConnector);

        Connector clientConnector = RESOLVER.resolve(
                "netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(clientConnector);

        // server side
        Consumer serverConsumer = serverConnector.getConsumer().get();
        Producer serverProducer = serverConnector.getProducer().get();

        // client side
        Producer clientProducer = clientConnector.getProducer().get();
        Consumer clientReceiver = clientConnector.getConsumer().get();

        final CountDownLatch doneSignal = new CountDownLatch(2);

        System.out.println("Subscribe to server consumer");
        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            BValue routingId = msg.getRoutingId().orElse(BValue.of(-1));
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " server] - socket open, routing id: " + routingId);
                break;
            case "close":
                System.out.println("[" + transport + " server] - socket closed, routing id: " + routingId);
                doneSignal.countDown();
                break;
            case "message":
                System.out.println("[" + transport + " server] - got message from routing id " + routingId + ": "
                        + msg.getPayload().toBArray()
                        + " --> close client connection (by send a null-payload msg) right now...");

                try {
                    serverProducer.sendWithAck(Message.of(BValue.of(routingId), null)).get();
                } catch (PromiseException | InterruptedException e) {
                    throw new RuntimeException("Error while try to close connection", e);
                }

                break;
            }
        });

        clientReceiver.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "close":
                System.out.println("[" + transport + " client] - connection closed");
                doneSignal.countDown();
                break;
            case "open":
                System.out.println("[" + transport + " client] - connection established");
                break;
            }
        });

        ((FailureHandlerAware<?>) clientReceiver).setFailureHandler((cause) -> {
            System.err.println("[" + transport + " client] - exception...");
            cause.printStackTrace();
        });

        serverConnector.start();
        clientConnector.start();

        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        assertTrue(serverConsumer instanceof FailureHandlerAware<?>);
        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            System.err.println("[" + transport + " server] - exception...");
            cause.printStackTrace();
            exceptionRef.set(cause);
            doneSignal.countDown();
        });

        clientProducer.sendWithAck(Message.of(Payload.of(BValue.of(TEXT)))).get();

        doneSignal.await();

        assertNull(exceptionRef.get());

        serverConnector.stop();
        clientConnector.stop();
    }

    protected void testHandlerException(@NonNull String transport, String path)
            throws InterruptedException, PromiseException {

        System.out.println("****** Netty4 " + transport + " - test server side handle exception");
        final String host = "localhost:8889";

        Connector serverConnector = RESOLVER.resolve(
                "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(serverConnector);

        Connector clientConnector = RESOLVER.resolve(
                "netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(clientConnector);

        // server side
        Consumer serverConsumer = serverConnector.getConsumer().get();

        // client side
        Producer clientProducer = clientConnector.getProducer().get();

        System.out.println("Subscribe to server consumer");
        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " server] - socket open, routing id: " + msg.getRoutingId().get());
                break;
            case "close":
                System.out.println(
                        "[" + transport + " server] - socket closed, routing id: " + msg.getRoutingId().get());
                break;
            case "message":
                String received = msg.body().asValue().getString();
                throw new RuntimeException(received);
            }
        });

        serverConnector.start();
        clientConnector.start();

        assertTrue(serverConsumer instanceof FailureHandlerAware<?>);
        final CountDownLatch doneSignal = new CountDownLatch(1);
        final AtomicReference<String> receivedRef = new AtomicReference<>(null);
        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            receivedRef.set(cause.getMessage());
            doneSignal.countDown();
        });

        clientProducer.sendWithAck(Message.of(Payload.of(BValue.of(TEXT)))).get();

        doneSignal.await();

        assertEquals(TEXT, receivedRef.get());

        serverConnector.stop();
        clientConnector.stop();
    }

    protected void testPingPong(@NonNull String transport, String path) throws InterruptedException, PromiseException {
        System.out.println("****** Netty4 " + transport + " - test ping/pong");
        final String host = "localhost:8889";

        Connector serverConnector = RESOLVER.resolve(
                "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(serverConnector);

        Connector clientConnector = RESOLVER.resolve(
                "netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
        assertNetty4Connector(clientConnector);

        // server side
        Consumer serverConsumer = serverConnector.getConsumer().get();
        Producer serverResponder = serverConnector.getProducer().get();

        // client side
        Producer clientProducer = clientConnector.getProducer().get();
        Consumer clientReceiver = clientConnector.getConsumer().get();

        final CountDownLatch doneSignal = new CountDownLatch(1);

        System.out.println("Subscribe to server consumer");
        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " server] - socket open, routing id: " + msg.getRoutingId().get());
                break;
            case "close":
                System.out.println(
                        "[" + transport + " server] - socket closed, routing id: " + msg.getRoutingId().get());
                break;
            default:
                System.out.println("[" + transport + " server] - got msg from source: " + msg.getMisc().get("source"));
                serverResponder.send(msg);
            }
        });

        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            cause.printStackTrace();
            doneSignal.countDown();
            return null;
        });

        final AtomicReference<String> receivedText = new AtomicReference<>(null);

        clientReceiver.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "open":
                System.out.println("[" + transport + " client] - connection established");
                break;
            case "close":
                System.out.println("[" + transport + " client] - connection closed");
                break;
            default:
                System.out.println("[" + transport + " client] - got msg from source: " + msg.getMisc().get("source")
                        + " -> payload: " + msg.getPayload().toBArray());
                receivedText.set(msg.body().asValue().getString());
                doneSignal.countDown();
            }
        });

        System.out.println("Start client and server...");
        serverConnector.start();
        clientConnector.start();

        clientProducer.sendWithAck(Message.of(Payload.of(BValue.of(TEXT)))).get();

        doneSignal.await();

        assertEquals(TEXT, receivedText.get());

        serverConnector.stop();
        clientConnector.stop();
    }
}
