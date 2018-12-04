package io.gridgo.connector.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.test.support.TestConnector;
import io.gridgo.connector.test.support.TestConsumer;
import io.gridgo.connector.test.support.TestProducer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.impl.SimpleRegistry;

public class ConnectorUnitTest {

	@Test
	public void testMultiSchemes() {
		var connector = new DefaultConnectorFactory().createConnector("test:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof TestConnector);

		connector = new DefaultConnectorFactory().createConnector("test1:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof TestConnector);
	}

	@Test
	public void testRegistrySubstitution() {
		var registry = new SimpleRegistry() //
				.register("host", "localhost") //
				.register("port", "8080") //
				.register("v1", "value1") //
				.register("v2", "value2");
		var factory = new DefaultConnectorFactory();
		factory.setRegistry(registry);

		var connector = (TestConnector) factory.createConnector("test:pull:tcp://${host}:${port}?p1=${v1}&p2=${v2}");
		Assert.assertEquals("localhost", connector.getPlaceholderPublic("host"));
		Assert.assertEquals("8080", connector.getPlaceholderPublic("port"));
		Assert.assertEquals("value1", connector.getParamPublic("p1"));
		Assert.assertEquals("value2", connector.getParamPublic("p2"));
	}

	@Test
	public void testConsumer() {
		var connector = (TestConnector) new DefaultConnectorFactory()
				.createConnector("test:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");

		Assert.assertEquals("v1", connector.getParamPublic("p1"));
		Assert.assertEquals("bar", connector.getParamPublic("foo", "bar"));
		Assert.assertEquals("pull", connector.getPlaceholderPublic("type"));
		Assert.assertEquals("tcp", connector.getPlaceholderPublic("transport"));

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

	@Test
	public void testProducer() {
		var connector = new DefaultConnectorFactory().createConnector("test:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
		var producer = (TestProducer) connector.getProducer().orElseThrow();
		connector.start();

		producer.send(null);

		var sendLatch = new CountDownLatch(1);
		producer.sendWithAck(null).fail(ex -> {
			if ("test exception".equals(ex.getMessage()))
				sendLatch.countDown();
		});
		try {
			sendLatch.await();
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

		var callLatch = new CountDownLatch(1);
		producer.call(Message.newDefault(Payload.newDefault(BValue.newDefault(1)))).done(response -> {
			if (response.getPayload().getBody().asValue().getInteger() == 2)
				callLatch.countDown();
		});
		try {
			callLatch.await();
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

		connector.stop();
	}
}
