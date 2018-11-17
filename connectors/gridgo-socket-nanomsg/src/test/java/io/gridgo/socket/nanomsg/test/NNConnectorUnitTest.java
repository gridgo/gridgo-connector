package io.gridgo.socket.nanomsg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.nanomsg.NNConnector;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class NNConnectorUnitTest {
	@Test
	public void testPairDuplex() throws InterruptedException, PromiseException {

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		System.out.println("Test pair duplex...");
		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

		String port = "8080";
		String host = "localhost";
		String address = host + ":" + port;

		Connector connector1 = resolver.resolve("nanomsg:pair:tcp:bind://" + address);
		assertNotNull(connector1);
		assertNotNull(connector1.getConnectorConfig());
		assertNotNull(connector1.getConnectorConfig().getRemaining());
		assertNotNull(connector1.getConnectorConfig().getParameters());
		assertTrue(connector1 instanceof NNConnector);
		assertEquals("pair:tcp:bind://" + address, connector1.getConnectorConfig().getRemaining());
		assertEquals("pair", connector1.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector1.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("bind", connector1.getConnectorConfig().getPlaceholders().get("role"));
		assertEquals(host, connector1.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals(port, connector1.getConnectorConfig().getPlaceholders().get("port"));

		Connector connector2 = resolver.resolve("nanomsg:pair:tcp:connect://" + address);
		assertNotNull(connector2);
		assertNotNull(connector2.getConnectorConfig());
		assertNotNull(connector2.getConnectorConfig().getRemaining());
		assertNotNull(connector2.getConnectorConfig().getParameters());
		assertTrue(connector2 instanceof NNConnector);
		assertEquals("pair:tcp:connect://" + address, connector2.getConnectorConfig().getRemaining());
		assertEquals("pair", connector2.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector2.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("connect", connector2.getConnectorConfig().getPlaceholders().get("role"));
		assertEquals(host, connector2.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals(port, connector2.getConnectorConfig().getPlaceholders().get("port"));

		connector1.start();
		assertTrue(connector1.getConsumer().isPresent());
		assertTrue(connector1.getProducer().isPresent());

		connector2.start();
		assertTrue(connector2.getConsumer().isPresent());
		assertTrue(connector2.getProducer().isPresent());

		Producer responder = connector1.getProducer().get();
		connector1.getConsumer().get().subscribe((message) -> {
			responder.send(message);
		});

		AtomicReference<String> pongDataRef = new AtomicReference<String>(null);
		final String data = "This is test text";

		final CountDownLatch doneSignal = new CountDownLatch(1);
		connector2.getConsumer().get().subscribe((message) -> {
			pongDataRef.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		connector2.getProducer().get().send(Message.newDefault(Payload.newDefault(BValue.newDefault(data))));

		doneSignal.await();
		assertEquals(data, pongDataRef.get());

		connector1.stop();
		connector2.stop();
	}

	@Test
	public void testSimpleTcp() throws InterruptedException, PromiseException {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

		Connector connector = resolver.resolve("nanomsg:pull:tcp://localhost:8080");
		assertNotNull(connector);
		assertNotNull(connector.getConnectorConfig());
		assertNotNull(connector.getConnectorConfig().getRemaining());
		assertNotNull(connector.getConnectorConfig().getParameters());
		assertTrue(connector instanceof NNConnector);
		assertEquals("pull:tcp://localhost:8080", connector.getConnectorConfig().getRemaining());
		assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));

		connector.start();

		Consumer consumer = connector.getConsumer().get();
		assertNotNull(consumer);

		Connector connector2 = resolver.resolve(
				"nanomsg:push:tcp://localhost:8080?batchingEnabled=true&maxBatchSize=2000&ringBufferSize=2048");
		assertNotNull(connector2);
		assertNotNull(connector2.getConnectorConfig());
		assertNotNull(connector2.getConnectorConfig().getRemaining());
		assertNotNull(connector2.getConnectorConfig().getParameters());
		assertTrue(connector2 instanceof NNConnector);

		assertEquals("push:tcp://localhost:8080", connector2.getConnectorConfig().getRemaining());
		assertEquals("push", connector2.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector2.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector2.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector2.getConnectorConfig().getPlaceholders().get("port"));

		assertEquals("true", connector2.getConnectorConfig().getParameters().get("batchingEnabled"));
		assertEquals("2000", connector2.getConnectorConfig().getParameters().get("maxBatchSize"));
		assertEquals("2048", connector2.getConnectorConfig().getParameters().get("ringBufferSize"));

		connector2.start();

		Producer producer = connector2.getProducer().get();
		assertNotNull(producer);

		warmUp(consumer, producer);

		try {
			this.doFnFSend(consumer, producer);
			this.doAckSend(consumer, producer);
		} finally {
			connector.stop();
			connector2.stop();
		}
	}

	private void warmUp(Consumer consumer, Producer producer) throws PromiseException, InterruptedException {
		producer.sendWithAck(Message.newDefault(Payload.newDefault(BObject.newFromSequence("cmd", "start")))).get();
	}

	private void doFnFSend(Consumer consumer, Producer producer) throws InterruptedException {
		int numMessages = (int) 1e2;
		CountDownLatch doneSignal = new CountDownLatch(numMessages);

		consumer.subscribe((message) -> {
			doneSignal.countDown();
		});
		long start = System.nanoTime();
		for (int i = 0; i < numMessages; i++) {
			producer.send(Message.newDefault(Payload.newDefault(BObject.newFromSequence("index", i))));
		}

		doneSignal.await();
		double elapsed = Double.valueOf(System.nanoTime() - start);
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println("FnF TRANSMITION DONE (*** not improved), " + numMessages + " messages were transmited in "
				+ df.format(elapsed / 1e6) + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "msg/s");
	}

	private void doAckSend(Consumer consumer, Producer producer) throws InterruptedException, PromiseException {
		int numMessages = (int) 1e2;
		CountDownLatch doneSignal = new CountDownLatch(numMessages);

		consumer.subscribe((message) -> {
			doneSignal.countDown();
		});

		long start = System.nanoTime();
		for (int i = 0; i < numMessages; i++) {
			producer.sendWithAck(Message.newDefault(Payload.newDefault(BObject.newFromSequence("index", i)))).get();
		}

		doneSignal.await();
		double elapsed = Double.valueOf(System.nanoTime() - start);
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println("ACK TRANSMITION DONE (*** not improved), " + numMessages + " messages were transmited in "
				+ df.format(elapsed / 1e6) + "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "msg/s");
	}
}
