package io.gridgo.connector.mongodb.test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.Document;
import org.joo.promise4j.Promise;
import org.junit.Assert;
import org.junit.Test;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.client.model.Filters;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.mongodb.MongoDBConstants;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.impl.SimpleRegistry;

public class MongoDBUnitTest {

    private static final int NUM_MESSAGES = 100;

    private Message createCountMessage() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_COUNT);
        return Message.of(Payload.of(headers, null));
    }

    private Message createDeleteManyRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_DELETE_MANY)
                             .set(MongoDBConstants.FILTER, BReference.of(Filters.gt("key", 1)));
        return Message.of(Payload.of(headers, null));
    }

    private Message createDeleteRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_DELETE_ONE)
                             .set(MongoDBConstants.FILTER, BReference.of(Filters.eq("key", 1)));
        return Message.of(Payload.of(headers, null));
    }

    private Message createFindAllRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_FIND_ALL)
                             .set(MongoDBConstants.FILTER, BReference.of(Filters.eq("name", "Hello")));
        return Message.of(Payload.of(headers, null));
    }

    private Message createFindByIdRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_FIND_BY_ID)
                             .setAny(MongoDBConstants.ID_FIELD, "key");
        return Message.of(Payload.of(headers, BValue.of(1)));
    }

    private Message createInsertMessage() {
        var doc1 = new Document("key", 4).append("type", "database").append("name", "Hello").append("info",
                new Document("x", 203).append("y", 102));
        BReference ref = BReference.of(doc1);
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_INSERT);
        return Message.of(Payload.of(headers, ref));
    }

    private Message createInsertMessages() {
        var doc1 = new Document("key", 1).append("type", "database").append("name", "Hello").append("info",
                new Document("x", 203).append("y", 102));
        var doc2 = new Document("key", 2).append("type", "database").append("name", "Hello").append("info",
                new Document("x", 203).append("y", 102));
        var doc3 = new Document("key", 3).append("type", "database").append("name", "World").append("info",
                new Document("x", 203).append("y", 102));
        BReference[] list = new BReference[] { BReference.of(doc1), BReference.of(doc2), BReference.of(doc3) };
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_INSERT);
        return Message.of(Payload.of(headers, BArray.of(list)));
    }

    private Message createUpdateManyRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_UPDATE_MANY)
                             .set(MongoDBConstants.FILTER, BReference.of(Filters.eq("name", "World")));
        var body = BReference.of(new Document("$set", new Document("name", "Hello")));
        return Message.of(Payload.of(headers, body));
    }

    private Message createUpdateRequest() {
        var headers = BObject.ofEmpty().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_UPDATE_ONE)
                             .set(MongoDBConstants.FILTER, BReference.of(Filters.eq("name", "Hello")));
        var body = BReference.of(new Document("$set", new Document("name", "World")));
        return Message.of(Payload.of(headers, body));
    }

    @Test
    public void testSimple() throws IOException, InterruptedException {
        String bindIp = "127.0.0.1";
        int port = 27017;
        String databaseName = "test";

        long started = System.nanoTime();
        MongoClient mongo = MongoClients.create("mongodb://" + bindIp + ":" + port + "/?waitQueueMultiple=10");
        long elapsed = System.nanoTime() - started;
        System.out.println("MongoClient created. Elapsed: " + elapsed / 1e6 + "ms");
        var latch = new CountDownLatch(1);
        var started1 = System.nanoTime();
        mongo.getDatabase(databaseName).getCollection("testCols").drop((a, b) -> {
            long elapsed1 = System.nanoTime() - started1;
            System.out.println("Drop collection completed. Elapsed: " + elapsed1 / 1e6 + "ms");
            long started2 = System.nanoTime();
            mongo.getDatabase(databaseName).createCollection("testCols", (result, throwable) -> {
                long elapsed2 = System.nanoTime() - started2;
                System.out.println("Create collection completed. Elapsed: " + elapsed2 / 1e6 + "ms");
                latch.countDown();
            });
        });
        latch.await();

        var registry = new SimpleRegistry().register("mongoClient", mongo);
        var context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        var connector = new DefaultConnectorFactory().createConnector("mongodb:mongoClient/test/testCols", context);
        connector.start();
        var producer = connector.getProducer().orElseThrow();

        var exRef = new AtomicReference<>();

        var callLatch = new CountDownLatch(1);
        producer.call(createInsertMessages()) //
                .pipeDone(msg -> producer.call(createInsertMessage())) //
                .pipeDone(msg -> producer.call(createCountMessage())) //
                .pipeDone(msg -> checkCount(msg, 4)) //
                .pipeDone(msg -> producer.call(createFindByIdRequest())) //
                .pipeDone(this::checkFindById) //
                .pipeDone(msg -> producer.call(createFindAllRequest())) //
                .pipeDone(msg -> checkFindAll(msg, 3)) //
                .pipeDone(msg -> producer.call(createDeleteRequest())) //
                .pipeDone(msg -> producer.call(createCountMessage())) //
                .pipeDone(msg -> checkCount(msg, 3)) //
                .pipeDone(msg -> producer.call(createUpdateRequest()))
                .pipeDone(msg -> producer.call(createFindAllRequest())) //
                .pipeDone(msg -> checkFindAll(msg, 1)) //
                .pipeDone(msg -> producer.call(createUpdateManyRequest()))
                .pipeDone(msg -> producer.call(createFindAllRequest())) //
                .pipeDone(msg -> checkFindAll(msg, 3)) //
                .pipeDone(msg -> producer.call(createDeleteManyRequest())) //
                .pipeDone(msg -> producer.call(createCountMessage())) //
                .pipeDone(msg -> checkCount(msg, 0)) //
                .pipeDone(msg -> producer.call(createInsertMessage())) //
                .done(msg -> callLatch.countDown()) //
                .fail(ex -> {
                    exRef.set(ex);
                    callLatch.countDown();
                });
        callLatch.await();

        Assert.assertNull(exRef.get());

        // test perf
        var failure = new AtomicInteger(0);
        var perfLatch = new AtomicInteger(NUM_MESSAGES);
        long perfStarted = System.nanoTime();
        for (int i = 0; i < NUM_MESSAGES; i++)
            producer.call(createFindByIdRequest()) //
                    .always((status, msg, ex) -> perfLatch.decrementAndGet()) //
                    .fail(ex -> {
                        ex.printStackTrace();
                        failure.incrementAndGet();
                    });
        while (perfLatch.get() != 0) {
            Thread.onSpinWait();
        }
        long perfElapsed = System.nanoTime() - perfStarted;
        System.out.println("Failures: " + failure.get());
        DecimalFormat df = new DecimalFormat("###,###.##");
        System.out.println("MongoDB findById done, " + NUM_MESSAGES + " messages were transmited in "
                + df.format(perfElapsed / 1e6) + "ms -> pace: " + df.format(1e9 * NUM_MESSAGES / perfElapsed)
                + "msg/s");

        connector.stop();
        mongo.close();

        Assert.assertEquals(0, failure.get());
    }

    private Promise<Message, Exception> checkFindAll(Message msg, int expected) {
        System.out.println("check find all");
        var doc = msg.body().asArray();
        if (doc != null && doc.size() == expected)
            return Promise.of(msg);
        return Promise.ofCause(new RuntimeException());
    }

    private Promise<Message, Exception> checkFindById(Message msg) {
        System.out.println("check find by id");
        var doc = msg.body().asObject();
        if (doc != null)
            return Promise.of(msg);
        return Promise.ofCause(new RuntimeException());
    }

    private Promise<Message, Exception> checkCount(Message msg, int expected) {
        System.out.println("check count");
        long count = msg.body().asValue().getLong();
        if (count == expected)
            return Promise.of(msg);
        return Promise.ofCause(new RuntimeException());
    }
}
