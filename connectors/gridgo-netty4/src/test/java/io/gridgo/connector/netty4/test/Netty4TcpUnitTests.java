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
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class Netty4TcpUnitTests {

	private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");
	private final static String TEXT = "this is test text";

	private void assertNetty4Connector(Connector connector) {
		assertNotNull(connector);
		assertTrue(connector instanceof Netty4Connector);
	}

	@Test
	public void testTCP() throws InterruptedException, PromiseException {

		final String host = "localhost:8888";

		Connector serverConnector = RESOLVER.resolve("netty4:server:tcp://" + host + "?workerThreads=2&bootThreads=2");
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER.resolve("netty4:client:tcp://" + host + "?workerThreads=2");
		assertNetty4Connector(clientConnector);

		serverConnector.start();
		clientConnector.start();

		Consumer server = serverConnector.getConsumer().get();
		assertNotNull(server);
		Producer client = clientConnector.getProducer().get();
		assertNotNull(client);

		final AtomicReference<String> receivedText = new AtomicReference<>(null);
		final CountDownLatch doneSignal = new CountDownLatch(1);
		server.subscribe((msg) -> {
			System.out.println("TCP server got message: " + msg.getPayload().getBody());
			receivedText.set(msg.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		client.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		System.out.println("sending done");
		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		serverConnector.stop();
		clientConnector.stop();
	}

	@Test
	public void testTcpPingPong() throws InterruptedException, PromiseException {
		final String host = "localhost:8889";

		Connector serverConnector = RESOLVER.resolve("netty4:server:tcp://" + host + "?workerThreads=2&bootThreads=2");
		assertNetty4Connector(serverConnector);

		Connector clientConnector = RESOLVER.resolve("netty4:client:tcp://" + host + "?workerThreads=2");
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
			System.out.println(
					"Got ping message from routingId " + msg.getRoutingId() + ": " + msg.getPayload().toBArray());
			serverResponder.send(msg);
		});

		final AtomicReference<String> receivedText = new AtomicReference<>(null);

		clientReceiver.subscribe((msg) -> {
			System.out.println("Got pong message: " + msg.getPayload().toBArray());
			receivedText.set(msg.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		clientProducer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		System.out.println("sending done");
		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		serverConnector.stop();
		clientConnector.stop();
	}
}
