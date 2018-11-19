package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQConnectorUnitTest {

	private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

	private static final String TEXT = "This is test text";

	private static final int port = 8080;
	private static final String host = "localhost";
	private static final String address = host + ":" + port;

	@Test
	public void testPubSubTCP() throws Exception {
		System.out.println("Test pub/sub via TCP");
		testPubSub("tcp", "localhost:5555");
	}

	@Test
//	@Ignore
	public void testPubSubPGM() throws Exception {
		System.out.println("Test pub/sub via PGM");
		testPubSub("pgm", "224.2.3.4:5555");
	}

	private void testPubSub(String transport, String address) throws Exception {
		Connector pubConnector = RESOLVER.resolve("zmq:pub:" + transport + "://" + address);
		Connector sub1Connector = RESOLVER.resolve("zmq:sub:" + transport + "://" + address + "?topic=topic1");
		Connector sub2Connector = RESOLVER.resolve("zmq:sub:" + transport + "://" + address + "?topic=topic2");

		try {
			pubConnector.start();
			assertTrue(pubConnector.getProducer().isPresent());

			sub1Connector.start();
			assertTrue(sub1Connector.getConsumer().isPresent());

			sub2Connector.start();
			assertTrue(sub2Connector.getConsumer().isPresent());

			Producer publisher = pubConnector.getProducer().get();
			Consumer subscriber1 = sub1Connector.getConsumer().get();
			Consumer subscriber2 = sub2Connector.getConsumer().get();

			final String text1 = TEXT + 1;
			final String text2 = TEXT + 2;

			final AtomicReference<String> recv1 = new AtomicReference<String>();
			final AtomicReference<String> recv2 = new AtomicReference<String>();

			final CountDownLatch doneSignal = new CountDownLatch(2);

			subscriber1.subscribe((msg) -> {
				String body = msg.getPayload().getBody().asValue().getString();
				System.out.println("Got msg: " + body);
				recv1.set(body);
				doneSignal.countDown();
			});

			subscriber2.subscribe((msg) -> {
				String body = msg.getPayload().getBody().asValue().getString();
				System.out.println("Got msg: " + body);
				recv2.set(body);
				doneSignal.countDown();
			});

			// publish data
			publisher.send(
					Message.newDefault(Payload.newDefault(BValue.newDefault(text1))).setRoutingIdFromAny("topic1"));
			publisher.send(
					Message.newDefault(Payload.newDefault(BValue.newDefault(text2))).setRoutingIdFromAny("topic2"));

			doneSignal.await(5, TimeUnit.SECONDS);

			assertEquals(text1, recv1.get());
			assertEquals(text2, recv2.get());

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			pubConnector.stop();
			sub1Connector.stop();
			sub2Connector.stop();
		}
	}

	@Test
	public void testPairPingPong() throws InterruptedException, PromiseException {

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		System.out.println("Test pair ping pong...");

		Connector connector1 = RESOLVER.resolve("zmq:pair:tcp:bind://" + address);
		Connector connector2 = RESOLVER.resolve("zmq:pair:tcp:connect://" + address);

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

		final CountDownLatch doneSignal = new CountDownLatch(1);
		connector2.getConsumer().get().subscribe((message) -> {
			pongDataRef.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		connector2.getProducer().get().send(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT))));

		doneSignal.await();
		assertEquals(TEXT, pongDataRef.get());

		connector1.stop();
		connector2.stop();
	}

	@Test
	public void testPairOneWay() throws InterruptedException, PromiseException {

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		System.out.println("Test pair oneway...");

		Connector connector1 = RESOLVER.resolve("zmq:pair:tcp:bind://" + address);
		Connector connector2 = RESOLVER.resolve("zmq:pair:tcp:connect://" + address);

		connector1.start();
		assertTrue(connector1.getConsumer().isPresent());
		assertTrue(connector1.getProducer().isPresent());

		connector2.start();
		assertTrue(connector2.getConsumer().isPresent());
		assertTrue(connector2.getProducer().isPresent());

		final CountDownLatch doneSignal = new CountDownLatch(2);

		AtomicReference<String> recvDataRef1 = new AtomicReference<String>(null);
		AtomicReference<String> recvDataRef2 = new AtomicReference<String>(null);

		connector1.getConsumer().get().subscribe((message) -> {
			recvDataRef2.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		connector2.getConsumer().get().subscribe((message) -> {
			recvDataRef1.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		connector1.getProducer().get().send(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT + 1))));
		connector2.getProducer().get().send(Message.newDefault(Payload.newDefault(BValue.newDefault(TEXT + 2))));

		doneSignal.await();

		assertEquals(TEXT + 1, recvDataRef1.get());
		assertEquals(TEXT + 2, recvDataRef2.get());

		connector1.stop();
		connector2.stop();
	}

	@Test
	public void testMonoplex() throws InterruptedException, PromiseException {

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

		System.out.println("Test tcp monoplex...");

		Connector connector1 = RESOLVER.resolve("zmq:pull:tcp://" + address);

		String queryString = "batchingEnabled=true&maxBatchSize=2000&ringBufferSize=2048";
		Connector connector2 = RESOLVER.resolve("zmq:push:tcp://" + address + "?" + queryString);

		connector1.start();
		assertTrue(connector1.getConsumer().isPresent());
		Consumer consumer = connector1.getConsumer().get();

		connector2.start();
		assertTrue(connector2.getProducer().isPresent());
		Producer producer = connector2.getProducer().get();

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
