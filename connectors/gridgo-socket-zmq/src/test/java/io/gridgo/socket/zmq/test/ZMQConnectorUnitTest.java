package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.zmq.ZMQConnector;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQConnectorUnitTest {

	@Test
	public void testPairDuplex() throws InterruptedException, PromiseException {

		System.out.println("Test pair duplex...");

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

		String port = "8889";
		String host = "localhost";
		String address = host + ":" + port;

		Connector connector1 = resolver.resolve("zmq:pair:tcp:bind://" + address);
		assertNotNull(connector1);
		assertNotNull(connector1.getConnectorConfig());
		assertNotNull(connector1.getConnectorConfig().getRemaining());
		assertNotNull(connector1.getConnectorConfig().getParameters());
		assertTrue(connector1 instanceof ZMQConnector);
		assertEquals("pair:tcp:bind://" + address, connector1.getConnectorConfig().getRemaining());
		assertEquals("pair", connector1.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector1.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("bind", connector1.getConnectorConfig().getPlaceholders().get("role"));
		assertEquals(host, connector1.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals(port, connector1.getConnectorConfig().getPlaceholders().get("port"));

		Connector connector2 = resolver.resolve("zmq:pair:tcp:connect://" + address);
		assertNotNull(connector2);
		assertNotNull(connector2.getConnectorConfig());
		assertNotNull(connector2.getConnectorConfig().getRemaining());
		assertNotNull(connector2.getConnectorConfig().getParameters());
		assertTrue(connector2 instanceof ZMQConnector);
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

		connector1.stop();
		connector2.stop();
	}

	// @Test
	public void testMonoplex() throws InterruptedException, PromiseException {
		System.out.println("Test monoplex...");

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

		Connector connector1 = resolver.resolve("zmq:pull:tcp://localhost:8080");
		assertNotNull(connector1);
		assertNotNull(connector1.getConnectorConfig());
		assertNotNull(connector1.getConnectorConfig().getRemaining());
		assertNotNull(connector1.getConnectorConfig().getParameters());
		assertTrue(connector1 instanceof ZMQConnector);
		assertEquals("pull:tcp://localhost:8080", connector1.getConnectorConfig().getRemaining());
		assertEquals("pull", connector1.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector1.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector1.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector1.getConnectorConfig().getPlaceholders().get("port"));

		connector1.start();

		Consumer consumer = connector1.getConsumer().get();
		assertNotNull(consumer);

		Connector connector2 = resolver
				.resolve("zmq:push:tcp://localhost:8080?batchingEnabled=true&maxBatchSize=2000&ringBufferSize=2048");
		assertNotNull(connector2);
		assertNotNull(connector2.getConnectorConfig());
		assertNotNull(connector2.getConnectorConfig().getRemaining());
		assertNotNull(connector2.getConnectorConfig().getParameters());
		assertTrue(connector2 instanceof ZMQConnector);

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
			connector1.stop();
			connector2.stop();
		}
	}

	private void warmUp(Consumer consumer, Producer producer) throws PromiseException, InterruptedException {
		System.out.println("Started consumer and producer");
		CountDownLatch doneSignal = new CountDownLatch(1);
		consumer.subscribe((msg) -> {
			System.out.println("Got message from source: " + msg.getMisc().get("source"));
			doneSignal.countDown();
		});
		producer.send(Message.newDefault(Payload.newDefault(BObject.newFromSequence("cmd", "start"))));
		doneSignal.await();
		System.out.println("Warmup done");
		consumer.clearSubscribers();
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

		consumer.clearSubscribers();
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

		consumer.clearSubscribers();
	}
}
