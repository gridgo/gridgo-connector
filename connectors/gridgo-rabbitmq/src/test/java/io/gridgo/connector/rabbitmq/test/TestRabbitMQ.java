package io.gridgo.connector.rabbitmq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
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

    @FunctionalInterface
    static interface Consumer4Args<One, Two, Three, Four> {
        public void accept(One one, Two two, Three three, Four four);
    }

    private static final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

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
                receivedTextRef.set(message.body().asValue().getString());
                triggerDone.run();
            });

            producer.send(Message.of(Payload.of(BElement.ofAny(TEXT))));

            waitForDone.run();
            assertEquals(TEXT, receivedTextRef.get());

            connector.stop();
        });
    }

    @Test
    public void testPubSub() throws InterruptedException {
        System.out.println("Test pub/sub");
        Connector connector1 = RESOLVER.resolve("rabbitmq://localhost/testFanoutExchange?exchangeType=fanout");
        Connector connector2 = RESOLVER.resolve("rabbitmq://localhost/testFanoutExchange?exchangeType=fanout");

        connector1.start();

        Producer producer = connector1.getProducer().get();
        Consumer consumer1 = connector1.getConsumer().get();

        connector2.start();
        Consumer consumer2 = connector2.getConsumer().get();

        final AtomicReference<String> receivedTextRef1 = new AtomicReference<String>(null);
        final AtomicReference<String> receivedTextRef2 = new AtomicReference<String>(null);

        CountDownLatch doneSignal = new CountDownLatch(2);

        consumer1.subscribe((message, deferred) -> {
            receivedTextRef1.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        consumer2.subscribe((message, deferred) -> {
            receivedTextRef2.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        producer.send(Message.of(Payload.of(BElement.ofAny(TEXT))));

        doneSignal.await();
        assertEquals(TEXT, receivedTextRef1.get());
        assertEquals(TEXT, receivedTextRef2.get());

        connector1.stop();
        connector2.stop();
    }

    @Test
    public void testRoutingKey() throws InterruptedException {
        System.out.println("Test routing key");
        Connector connector1 = RESOLVER.resolve("rabbitmq://localhost/testDirectExchange?exchangeType=direct&routingKey=key1");

        Connector connector2 = RESOLVER.resolve("rabbitmq://localhost/testDirectExchange?exchangeType=direct&routingKey=key2");

        connector1.start();
        connector2.start();

        Producer producer1 = connector1.getProducer().get();
        Consumer consumer1 = connector1.getConsumer().get();

        Consumer consumer2 = connector2.getConsumer().get();

        final String text1 = TEXT + "1";
        final String text2 = TEXT + "2";

        final AtomicReference<String> receivedTextRef1 = new AtomicReference<String>(null);
        final AtomicReference<String> receivedTextRef2 = new AtomicReference<String>(null);

        CountDownLatch doneSignal = new CountDownLatch(2);

        consumer1.subscribe((message, deferred) -> {
            System.out.println("consumer 1: got message from source: " + message.getMisc().get("source"));
            receivedTextRef1.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        consumer2.subscribe((message, deferred) -> {
            System.out.println("consumer 2: got message from source: " + message.getMisc().get("source"));
            receivedTextRef2.set(message.body().asValue().getString());
            doneSignal.countDown();
        });

        producer1.send(Message.of(BValue.of("key1"), Payload.of(BElement.ofAny(text1))));
        producer1.send(Message.of(BValue.of("key2"), Payload.of(BElement.ofAny(text2))));

        doneSignal.await();
        assertEquals(text1, receivedTextRef1.get());
        assertEquals(text2, receivedTextRef2.get());

        connector1.stop();

        connector2.stop();
    }

    @Test
    public void testRoutingKeyRPC() throws InterruptedException, PromiseException {
        System.out.println("Test routing key rpc");
        Connector connector1 = RESOLVER.resolve("rabbitmq://localhost/testDirectExchangeRPC?exchangeType=direct&routingKey=key1&rpc=true");
        Connector connector2 = RESOLVER.resolve("rabbitmq://localhost/testDirectExchangeRPC?exchangeType=direct&routingKey=key2&rpc=true");

        connector1.start();
        connector2.start();

        Producer producer = connector1.getProducer().get();
        Consumer consumer1 = connector1.getConsumer().get();

        Consumer consumer2 = connector2.getConsumer().get();

        final String text1 = TEXT + "1";
        final String text2 = TEXT + "2";

        BiConsumer<Message, Deferred<Message, Exception>> echoMessageHandler = (message, deferred) -> {
            try {
                Payload responsePayload = Payload.of(message.body());
                deferred.resolve(Message.of(responsePayload));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        consumer1.subscribe(echoMessageHandler);
        consumer2.subscribe(echoMessageHandler);

        Message req1 = Message.of(BValue.of("key1"), Payload.of(BElement.ofAny(text1)));
        Message req2 = Message.of(BValue.of("key2"), Payload.of(BElement.ofAny(text2)));

        Message resp1 = producer.call(req1).get();
        Message resp2 = producer.call(req2).get();

        assertEquals(text1, resp1.body().asValue().getString());
        assertEquals(text2, resp2.body().asValue().getString());

        connector1.stop();

        connector2.stop();
    }

    @Test
    public void testRPC() throws InterruptedException {
        System.out.println("Test RPC");
        final Connector connector = RESOLVER.resolve("rabbitmq://localhost?queueName=test&rpc=true");
        init(connector, (producer, consumer, triggerDone, waitForDone) -> {

            consumer.subscribe((message, deferred) -> {
                System.out.println("got message from source: " + message.getMisc().get("source"));
                try {
                    Payload responsePayload = Payload.of(message.body());
                    deferred.resolve(Message.of(responsePayload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            final AtomicReference<String> receivedTextRef = new AtomicReference<String>(null);
            Message msg = Message.of(Payload.of(BElement.ofAny(TEXT)));
            Promise<Message, Exception> promise = producer.call(msg);
            promise.done((message) -> {
                receivedTextRef.set(message.body().asValue().getString());
                triggerDone.run();
            });

            waitForDone.run();
            assertEquals(TEXT, receivedTextRef.get());

            connector.stop();
        });
    }
}
