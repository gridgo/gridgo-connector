package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class TestPGM {

	private static final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

	private static final String TEXT = "This is test text";

	public static void main(String[] args) throws Exception {
		System.out.println("Test pub/sub via TCP");

		String transport = "epgm";
		String multicastIp = "239.192.1.1";
		String address = "en0;" + multicastIp + ":5555";

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

			doneSignal.await();

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
}
