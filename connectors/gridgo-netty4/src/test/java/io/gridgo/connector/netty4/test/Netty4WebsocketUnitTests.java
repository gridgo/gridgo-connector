package io.gridgo.connector.netty4.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

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

public class Netty4WebsocketUnitTests {

	private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");
	private final static String TEXT = "this is test text";

	public static void main(String[] args) throws InterruptedException, PromiseException {
		Connector connector = RESOLVER.resolve("netty4:server:ws://localhost:8888/test");
		connector.start();

		assertTrue(connector.getConsumer().isPresent());
		Consumer consumer = connector.getConsumer().get();

		assertTrue(connector.getProducer().isPresent());
		Producer producer = connector.getProducer().get();

		CountDownLatch doneSignal = new CountDownLatch(1);
		consumer.subscribe((msg) -> {
			System.out.println("Got ws message frrom channel " + msg.getRoutingId().orElse(null) + ": "
					+ msg.getPayload().getBody());

			// send back to client
			producer.send(msg);
		});

		doneSignal.await();
		connector.stop();
	}

	private void assertNetty4Connector(Connector connector) {
		assertNotNull(connector);
		assertTrue(connector instanceof Netty4Connector);
	}

	@Test
	public void testTcpPingPong() throws InterruptedException, PromiseException {
		final String host = "localhost:8889";

		Connector serverConnector = RESOLVER.resolve("netty4:server:ws://" + host + "?workerThreads=2&bootThreads=2");
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER.resolve("netty4:client:ws://" + host + "/" + "?workerThreads=2");
		assertNetty4Connector(clientConnector);

		serverConnector.start();
		clientConnector.start();

		assertTrue(serverConnector.getConsumer().isPresent());
		Consumer serverConsumer = serverConnector.getConsumer().get();

		assertTrue(serverConnector.getProducer().isPresent());
		Producer serverResponder = serverConnector.getProducer().get();

		assertTrue(clientConnector.getProducer().isPresent());
		Producer clientProducer = clientConnector.getProducer().get();

		assertTrue(clientConnector.getConsumer().isPresent());
		Consumer clientReceiver = clientConnector.getConsumer().get();

		final CountDownLatch doneSignal = new CountDownLatch(1);

		serverConsumer.subscribe((msg) -> {
			serverResponder.send(msg);
		});

		final AtomicReference<String> receivedText = new AtomicReference<>(null);

		clientReceiver.subscribe((msg) -> {
			receivedText.set(msg.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		clientProducer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		serverConnector.stop();
		clientConnector.stop();
	}

	@Test
	public void testHandlerException() throws InterruptedException, PromiseException {
		final String host = "localhost:8889";

		Connector serverConnector = RESOLVER.resolve("netty4:server:ws://" + host + "?workerThreads=2&bootThreads=2");
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER.resolve("netty4:client:ws://" + host + "?workerThreads=2");
		assertNetty4Connector(clientConnector);

		serverConnector.start();
		clientConnector.start();

		assertTrue(serverConnector.getConsumer().isPresent());
		Consumer serverConsumer = serverConnector.getConsumer().get();

		assertTrue(serverConnector.getProducer().isPresent());

		assertTrue(clientConnector.getProducer().isPresent());
		Producer clientProducer = clientConnector.getProducer().get();

		serverConsumer.subscribe((msg) -> {
			String received = msg.getPayload().getBody().asValue().getString();
			throw new RuntimeException(received);
		});

		assertTrue(serverConsumer instanceof FailureHandlerAware<?>);
		final CountDownLatch doneSignal = new CountDownLatch(1);
		final AtomicReference<String> receivedRef = new AtomicReference<>(null);
		((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
			receivedRef.set(cause.getMessage());
			doneSignal.countDown();
			return null;
		});

		clientProducer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		doneSignal.await();

		assertEquals(TEXT, receivedRef.get());

		serverConnector.stop();
		clientConnector.stop();
	}
}
