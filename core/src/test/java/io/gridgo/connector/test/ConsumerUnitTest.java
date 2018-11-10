package io.gridgo.connector.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.test.support.TestConsumer;

public class ConsumerUnitTest {

	@Test
	public void testConsumer() {
		var connector = new DefaultConnectorFactory().createConnector("test:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
		var consumer = (TestConsumer) connector.getConsumer().orElseThrow();
		connector.start();
		var latch = new CountDownLatch(1);
		consumer.subscribe(msg -> {
			if (msg.getPayload().getHeaders().getInteger("test-header") == 1)
				latch.countDown();
		});
		consumer.testPublish();
		try {
			latch.await();
		} catch (InterruptedException e) {

		}
		connector.stop();
	}
}
