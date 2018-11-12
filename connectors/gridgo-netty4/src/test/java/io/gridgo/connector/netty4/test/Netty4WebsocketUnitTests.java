package io.gridgo.connector.netty4.test;

import java.util.concurrent.CountDownLatch;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.utils.ThreadUtils;

public class Netty4WebsocketUnitTests {

	private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");
	// private final static String TEXT = "this is test text";

	@Test
	public void testTCP() throws InterruptedException, PromiseException {
		Connector connector = RESOLVER.resolve("netty4:ws://localhost:8888/test");
		connector.start();

		Consumer consumer = connector.getConsumer().get();

		CountDownLatch doneSignal = new CountDownLatch(1);
		consumer.subscribe((msg) -> {
			System.out.println("Got ws message: " + msg.getPayload().getBody());
			doneSignal.countDown();
		});

		doneSignal.await();

		connector.stop();

		ThreadUtils.sleep(1000);
	}
}
