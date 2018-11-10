package io.gridgo.connector.rabbitmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.Promise;
import org.junit.Test;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.rabbitmq.RabbitMQConnector;
import io.gridgo.connector.rabbitmq.RabbitMQConsumer;
import io.gridgo.connector.rabbitmq.RabbitMQProducer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class TestRabbitMQ {

	private static final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

	@FunctionalInterface
	static interface Consumer4Args<One, Two, Three, Four> {
		public void accept(One one, Two two, Three three, Four four);
	}

	private static final String TEXT = "This is message";

	private void init(Connector connector, Consumer4Args<Producer, Consumer, Runnable, Runnable> output) {
		assertNotNull(connector);
		assertTrue(connector instanceof RabbitMQConnector);

		connector.start();

		Producer producer = connector.getProducer().get();
		assertNotNull(producer);
		assertTrue(producer instanceof RabbitMQProducer);

		Consumer consumer = connector.getConsumer().get();
		assertNotNull(consumer);
		assertTrue(consumer instanceof RabbitMQConsumer);

		producer.start();
		consumer.start();
		final AtomicReference<CountDownLatch> doneSignal = new AtomicReference<CountDownLatch>(new CountDownLatch(1));

		final Runnable triggerDone = () -> {
			doneSignal.get().countDown();
			doneSignal.set(new CountDownLatch(1));
		};

		final Runnable waitForDone = () -> {
			try {
				doneSignal.get().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		output.accept(producer, consumer, triggerDone, waitForDone);
	}

	@Test
	public void testDirectQueue() throws InterruptedException {
		System.out.println("Test direct queue");
		final Connector connector = RESOLVER.resolve("rabbitmq://localhost?queueName=test");
		init(connector, (producer, consumer, triggerDone, waitForDone) -> {

			final AtomicReference<String> receivedTextRef = new AtomicReference<String>(null);

			consumer.subscribe((message, deferred) -> {
				receivedTextRef.set(message.getPayload().getBody().asValue().getString());
				triggerDone.run();
			});

			producer.send(Message.newDefault(Payload.newDefault(BElement.fromAny(TEXT))));

			waitForDone.run();
			assertEquals(TEXT, receivedTextRef.get());

			producer.stop();
			consumer.stop();

			connector.stop();
		});
	}

	@Test
	public void testRPC() throws InterruptedException {
		System.out.println("Test RPC");
		final Connector connector = RESOLVER.resolve("rabbitmq://localhost?queueName=test&rpc=true");
		init(connector, (producer, consumer, triggerDone, waitForDone) -> {

			consumer.subscribe((message, deferred) -> {
				try {
					Payload responsePayload = Payload.newDefault(message.getPayload().getBody());
					deferred.resolve(Message.newDefault(responsePayload));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			final AtomicReference<String> receivedTextRef = new AtomicReference<String>(null);
			Message msg = Message.newDefault(Payload.newDefault(BElement.fromAny(TEXT)));
			Promise<Message, Exception> promise = producer.call(msg);
			promise.done((message) -> {
				receivedTextRef.set(message.getPayload().getBody().asValue().getString());
				triggerDone.run();
			});

			waitForDone.run();
			assertEquals(TEXT, receivedTextRef.get());

			producer.stop();
			consumer.stop();

			connector.stop();
		});
	}

	@Test
	public void testPubSub() throws InterruptedException {
		System.out.println("Test pub/sub");
		Connector connector1 = RESOLVER.resolve("rabbitmq://localhost/testExchange?exchangeType=fanout");
		Producer producer1 = connector1.getProducer().get();
		Consumer consumer1 = connector1.getConsumer().get();

		Connector connector2 = RESOLVER.resolve("rabbitmq://localhost/testExchange?exchangeType=fanout");
		Consumer consumer2 = connector2.getConsumer().get();

		final AtomicReference<String> receivedTextRef1 = new AtomicReference<String>(null);
		final AtomicReference<String> receivedTextRef2 = new AtomicReference<String>(null);

		CountDownLatch doneSignal = new CountDownLatch(2);

		consumer1.subscribe((message, deferred) -> {
			receivedTextRef1.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		consumer2.subscribe((message, deferred) -> {
			receivedTextRef2.set(message.getPayload().getBody().asValue().getString());
			doneSignal.countDown();
		});

		producer1.send(Message.newDefault(Payload.newDefault(BElement.fromAny(TEXT))));

		doneSignal.await();
		assertEquals(TEXT, receivedTextRef1.get());
		assertEquals(TEXT, receivedTextRef2.get());

		producer1.stop();
		consumer1.stop();
		connector1.stop();

		consumer2.stop();
		connector2.stop();
	}
}
