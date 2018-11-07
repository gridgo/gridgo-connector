package io.gridgo.socket.nanomsg.test;

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
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.nanomsg.NNConnector;

public class TestNNConnector {

	@Test
	public void testSimpleTcp() throws InterruptedException, PromiseException {
		ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.socket.nanomsg");

		Connector connector = resolver.resolve("nanomsg:pull:tcp://localhost:8080?p1=v1&p2=v2");
		assertNotNull(connector);
		assertNotNull(connector.getConnectorConfig());
		assertNotNull(connector.getConnectorConfig().getRemaining());
		assertNotNull(connector.getConnectorConfig().getParameters());
		assertTrue(connector instanceof NNConnector);
		assertEquals("pull:tcp://localhost:8080", connector.getConnectorConfig().getRemaining());
		assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));

		Consumer consumer = connector.getConsumer().get();
		assertNotNull(consumer);

		connector = resolver.resolve("nanomsg:push:tcp://localhost:8080?p1=v1&p2=v2");
		assertNotNull(connector);
		assertNotNull(connector.getConnectorConfig());
		assertNotNull(connector.getConnectorConfig().getRemaining());
		assertNotNull(connector.getConnectorConfig().getParameters());
		assertTrue(connector instanceof NNConnector);
		assertEquals("push:tcp://localhost:8080", connector.getConnectorConfig().getRemaining());
		assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		assertEquals("push", connector.getConnectorConfig().getPlaceholders().get("type"));
		assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		assertEquals("localhost", connector.getConnectorConfig().getPlaceholders().get("host"));
		assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));

		Producer producer = connector.getProducer().get();
		assertNotNull(producer);

		warmUp(consumer, producer);

		try {
			this.doFnFSend(consumer, producer);
			this.doAckSend(consumer, producer);
		} finally {
			producer.stop();
			consumer.stop();
		}
	}

	private void warmUp(Consumer consumer, Producer producer) throws PromiseException, InterruptedException {
		consumer.start();
		producer.start();
		producer.sendWithAck(Message.newDefault(Payload.newDefault(BObject.newFromSequence("cmd", "start")))).get();
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
