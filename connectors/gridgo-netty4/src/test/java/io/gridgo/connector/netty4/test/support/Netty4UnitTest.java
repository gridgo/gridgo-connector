package io.gridgo.connector.netty4.test.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

public class Netty4UnitTest {
	private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");
	private final static String TEXT = "this is test text";

	private void assertNetty4Connector(Connector connector) {
		assertNotNull(connector);
		assertTrue(connector instanceof Netty4Connector);
		assertTrue(connector.getProducer().isPresent());
		assertTrue(connector.getConsumer().isPresent());
	}

	protected void testPingPong(@NonNull String transport, String path) throws InterruptedException, PromiseException {
		System.out.println("Netty4 " + transport + " - test ping/pong");
		final String host = "localhost:8889";

		Connector serverConnector = RESOLVER
				.resolve("netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER
				.resolve("netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
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
			if (msg.getPayload() != null) {
				serverResponder.send(msg);
			} else {
				String socketMessageType = (String) msg.getMisc().get("socketMessageType");
				switch (socketMessageType) {
				case "open":
					System.out.println(
							"[" + transport + " server] - socket open, routing id: " + msg.getRoutingId().get());
					break;
				case "close":
					System.out.println(
							"[" + transport + " server] - socket closed, routing id: " + msg.getRoutingId().get());
					break;
				}
			}
		});

		((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
			cause.printStackTrace();
			doneSignal.countDown();
			return null;
		});

		final AtomicReference<String> receivedText = new AtomicReference<>(null);

		clientReceiver.subscribe((msg) -> {
			if (msg.getPayload() != null) {
				receivedText.set(msg.getPayload().getBody().asValue().getString());
				doneSignal.countDown();
			} else {
				String socketMessageType = (String) msg.getMisc().get("socketMessageType");
				switch (socketMessageType) {
				case "open":
					System.out.println("[" + transport + " client] - connection established");
					break;
				case "close":
					System.out.println("[" + transport + " client] - connection closed");
					break;
				}
			}
		});

		System.out.println("Start client and server...");
		serverConnector.start();
		clientConnector.start();

		clientProducer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		serverConnector.stop();
		clientConnector.stop();
	}

	protected void testHandlerException(@NonNull String transport, String path)
			throws InterruptedException, PromiseException {

		System.out.println("Netty4 " + transport + " - test server side handle exception");
		final String host = "localhost:8889";

		Connector serverConnector = RESOLVER
				.resolve("netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER
				.resolve("netty4:client:" + transport + "://" + host + (path == null ? "" : ("/" + path)));
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
				System.out
						.println("[" + transport + " server] - socket closed, routing id: " + msg.getRoutingId().get());
				break;
			case "message":
				String received = msg.getPayload().getBody().asValue().getString();
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

		clientProducer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		doneSignal.await();

		assertEquals(TEXT, receivedRef.get());

		serverConnector.stop();
		clientConnector.stop();
	}
}
