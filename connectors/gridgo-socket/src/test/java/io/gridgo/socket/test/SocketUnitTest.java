package io.gridgo.socket.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class SocketUnitTest {

	@Test
	public void testSocket() throws InterruptedException {
		var factory = new DefaultConnectorFactory(new ClasspathConnectorResolver("io.gridgo.socket"));
		var connector1 = factory.createConnector("testsocket:pull:tcp://127.0.0.1:9102");
		var connector2 = factory.createConnector("testsocket:push:tcp://127.0.0.1:9102");

		var latch = new CountDownLatch(1);

		Assert.assertTrue(connector1.getProducer().isEmpty());
		Assert.assertTrue(connector1.getConsumer().isPresent());

		Assert.assertTrue(connector2.getConsumer().isEmpty());
		Assert.assertTrue(connector2.getProducer().isPresent());

		var exRef = new AtomicInteger();

		connector1.getConsumer().get().subscribe(msg -> {
			if (msg.getPayload().getBody().isValue())
				exRef.set(msg.getPayload().getBody().asValue().getInteger());
			latch.countDown();
		});

		connector1.start();
		connector2.start();

		connector2.getProducer().get().send(Message.newDefault(Payload.newDefault(BValue.newDefault(1))));

		latch.await();

		connector2.stop();
		connector1.stop();
	}
}
