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
import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.Producer;
import io.gridgo.connector.Responder;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.netty4.Netty4Connector;
import io.gridgo.connector.netty4.Netty4Consumer;
import io.gridgo.connector.netty4.Netty4Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class Netty4TcpUnitTests {

	private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");
	private final static String TEXT = "this is test text";

	@Test
	public void testTCP() throws InterruptedException, PromiseException {

		Connector connector = RESOLVER.resolve("netty4:tcp://localhost:8888?workerThreads=2&bootThreads=2");
		connector.start();

		Consumer consumer = connector.getConsumer().get();
		consumer.start();

		Producer producer = connector.getProducer().get();
		producer.start();

		final AtomicReference<String> receivedText = new AtomicReference<>(null);

		final CountDownLatch doneSignal = new CountDownLatch(1);

		consumer.subscribe((msg) -> {
			System.out.println("TCP server got message: " + msg.getPayload().getBody());
			receivedText.set(msg.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		producer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		System.out.println("sending done");
		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		producer.stop();
		consumer.stop();
		connector.stop();
	}

	@Test
	public void testTcpPingPong() throws InterruptedException, PromiseException {
		Connector connector = RESOLVER.resolve("netty4:tcp://localhost:8889?workerThreads=2&bootThreads=2");
		assertTrue(connector instanceof Netty4Connector);
		connector.start();

		Consumer consumer = connector.getConsumer().get();
		assertTrue(consumer instanceof Netty4Consumer);

		Responder responder = ((HasResponder) consumer).getResponder();
		assertNotNull(responder);

		Producer producer = connector.getProducer().get();
		assertTrue(producer instanceof Netty4Producer);

		final CountDownLatch doneSignal = new CountDownLatch(1);

		consumer.subscribe((msg) -> {
			System.out.println(
					"Got ping message from routingId " + msg.getRoutingId() + ": " + msg.getPayload().toBArray());
			responder.send(msg);
		});

		final AtomicReference<String> receivedText = new AtomicReference<>(null);

		((HasReceiver) producer).setReceiveCallback((msg) -> {
			System.out.println("Got pong message: " + msg.getPayload().toBArray());
			receivedText.set(msg.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		producer.sendWithAck(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT)))).get();

		System.out.println("sending done");
		doneSignal.await();

		assertEquals(TEXT, receivedText.get());

		producer.stop();
		consumer.stop();
		connector.stop();
	}
}
