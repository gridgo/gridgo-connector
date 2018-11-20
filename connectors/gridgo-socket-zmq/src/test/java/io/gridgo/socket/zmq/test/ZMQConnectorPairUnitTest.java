package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQConnectorPairUnitTest {

	private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

	private static final String TEXT = "This is test text";

	private static final int port = 8080;
	private static final String host = "localhost";
	private static final String address = host + ":" + port;

	@Test
	public void testPairOneWay() throws InterruptedException, PromiseException {

		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Windows"))
			return;

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
}
