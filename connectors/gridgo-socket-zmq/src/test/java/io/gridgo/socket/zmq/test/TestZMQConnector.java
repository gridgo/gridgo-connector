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

public class TestZMQConnector {

	@Test
	public void testSimpleTcp() throws InterruptedException, PromiseException {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector.zmq");
		Connector connector = resolver.resolve("zmq:pull:tcp://localhost:8080?p1=v1&p2=v2");
		assertNotNull(connector);
		assertNotNull(connector.getConnectorConfig());
		assertNotNull(connector.getConnectorConfig().getRemaining());
		assertNotNull(connector.getConnectorConfig().getParameters());
		assertTrue(connector instanceof ZMQConnector);
		assertEquals("pull:tcp://localhost:8080", connector.getConnectorConfig().getRemaining());
		assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));

		connector.start();

		Consumer consumer = connector.getConsumer().get();
		assertNotNull(consumer);

		resolver = new ClasspathConnectorResolver("io.gridgo.socket.zmq");
		Connector connector2 = resolver.resolve("zmq:push:tcp://localhost:8080?p1=v1&p2=v2");
		assertNotNull(connector2);
		assertNotNull(connector2.getConnectorConfig());
		assertNotNull(connector2.getConnectorConfig().getRemaining());
		assertNotNull(connector2.getConnectorConfig().getParameters());
		assertTrue(connector2 instanceof ZMQConnector);
		assertEquals("push:tcp://localhost:8080", connector2.getConnectorConfig().getRemaining());
		assertEquals("v1", connector2.getConnectorConfig().getParameters().get("p1"));
		assertEquals("v2", connector2.getConnectorConfig().getParameters().get("p2"));
		assertEquals("push", connector2.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector2.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector2.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector2.getConnectorConfig().getPlaceholders().get("port"));

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
		System.out.println("Started consumer and producer");
		producer.sendWithAck(Message.newDefault(Payload.newDefault(BObject.newFromSequence("cmd", "start")))).get();
		System.out.println("Warmup done");
	}

	private void doFnFSend(Consumer consumer, Producer producer) throws InterruptedException {
		int numMessages = (int) 1e5;
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
		int numMessages = (int) 1e3;
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
