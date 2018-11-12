package io.gridgo.connector.mongodb.test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;
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

		var callLatch = new CountDownLatch(1);
		producer.call(createInsertMessage()) //
				.pipeDone(msg -> producer.call(createCountMessage())) //
				.pipeDone(msg -> {
					System.out.println("check count");
					long count = msg.getPayload().getBody().asValue().getLong();
					if (count == 3)
						return new SimpleDonePromise<Message, Exception>(msg);
					return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
				}) //
				.pipeDone(msg -> producer.call(createFindByIdRequest())) //
				.pipeDone(msg -> {
					System.out.println("check find by id");
					var doc = msg.getPayload().getBody().asReference().getReference();
					if (doc != null && doc instanceof Document)
						return new SimpleDonePromise<Message, Exception>(msg);
					return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
				}) //
				.pipeDone(msg -> producer.call(createFindAllRequest())) //
				.pipeDone(msg -> {
					System.out.println("check find all");
					var doc = msg.getPayload().getBody().asArray();
					if (doc != null && doc.size() == 2)
						return new SimpleDonePromise<Message, Exception>(msg);
					return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
				}).done(msg -> callLatch.countDown());
		callLatch.await();

		// test perf
		var failure = new AtomicInteger(0);
		var perfLatch = new AtomicInteger(NUM_MESSAGES);
		long perfStarted = System.nanoTime();
		for (int i = 0; i < NUM_MESSAGES; i++)
			producer.call(createFindByIdRequest()).always((status, msg, ex) -> perfLatch.decrementAndGet()).fail(ex -> {
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
	}

	private Message createFindAllRequest() {
		var headers = BObject.newDefault().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_FIND_ALL)
				.set(MongoDBConstants.FILTER, BReference.newDefault(Filters.eq("name", "Hello")));
		return Message.newDefault(Payload.newDefault(headers, null));
	}

	private Message createFindByIdRequest() {
		var headers = BObject.newDefault().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_FIND_BY_ID)
				.setAny(MongoDBConstants.ID_FIELD, "key");
		return Message.newDefault(Payload.newDefault(headers, BValue.newDefault(1)));
	}

	private Message createInsertMessage() {
		var doc1 = new Document("key", 1).append("type", "database").append("name", "Hello").append("info",
				new Document("x", 203).append("y", 102));
		var doc2 = new Document("key", 2).append("type", "database").append("name", "Hello").append("info",
				new Document("x", 203).append("y", 102));
		var doc3 = new Document("key", 3).append("type", "database").append("name", "World").append("info",
				new Document("x", 203).append("y", 102));
		BReference[] list = new BReference[] { BReference.newDefault(doc1), BReference.newDefault(doc2),
				BReference.newDefault(doc3) };
		var headers = BObject.newDefault().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_INSERT);
		return Message.newDefault(Payload.newDefault(headers, BArray.newDefault(list)));
	}

	private Message createCountMessage() {
		var headers = BObject.newDefault().setAny(MongoDBConstants.OPERATION, MongoDBConstants.OPERATION_COUNT);
		return Message.newDefault(Payload.newDefault(headers, null));
	}
}
