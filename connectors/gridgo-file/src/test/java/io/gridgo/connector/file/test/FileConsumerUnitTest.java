package io.gridgo.connector.file.test;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.file.FileProducer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class FileConsumerUnitTest {

	private static final int NUM_MESSAGES = 2;
	private static final int LIMIT = 100;

	@Test
	public void testBatchWithLengthPrepend() throws InterruptedException {
		System.out.println("Test batching with length prepend\n");
		doTestFile("file:disruptor", "xml", "true", "true");
		doTestFile("file:disruptor", "json", "true", "true");
		doTestFile("file:disruptor", "raw", "true", "true");
		System.out.println("-----");
	}

	@Test
	public void testNonBatchWithLengthPrepend() throws InterruptedException {
		System.out.println("Test non-batching with length prepend\n");
		doTestFile("file:disruptor", "xml", "false", "true");
		doTestFile("file:disruptor", "json", "false", "true");
		doTestFile("file:disruptor", "raw", "false", "true");
		System.out.println("-----");
	}

	private void doTestFile(String scheme, String format, String batchEnabled, String lengthPrepend)
			throws InterruptedException {
		var totalSentBytes = prepareFile(scheme, format, batchEnabled, lengthPrepend);

		var endpoint = scheme + "://[test." + lengthPrepend + "." + batchEnabled + "." + format + "]?format=" + format
				+ "&limitSize=" + LIMIT + "&deleteOnShutdown=true&lengthPrepend=" + lengthPrepend;
		var strategy = new ExecutorExecutionStrategy(1);
		var context = new DefaultConnectorContextBuilder().setConsumerExecutionStrategy(strategy).build();
		var connector = new DefaultConnectorFactory().createConnector(endpoint, context);
		var consumer = connector.getConsumer().orElseThrow();
		var latch = new CountDownLatch(NUM_MESSAGES);
		var exRef = new AtomicReference<>();
		consumer.subscribe(msg -> {
			var response = msg.getPayload().getBody().asValue().getString();
			if (!"hello".equals(response))
				exRef.set(new RuntimeException("Expected: hello. Actual: " + response));
			latch.countDown();
		});
		var start = System.nanoTime();
		connector.start();
		latch.await();

		var elapsed = System.nanoTime() - start;
		printPace("Consumer (format=" + format + ", batchingEnabled=" + batchEnabled + ", lengthPrepend="
				+ lengthPrepend + ")\n", NUM_MESSAGES, elapsed, totalSentBytes);

		connector.stop();
		strategy.stop();
		Assert.assertNull(exRef.get());
	}

	private long prepareFile(String scheme, String format, String batchEnabled, String lengthPrepend)
			throws InterruptedException {
		var connector = new DefaultConnectorFactory()
				.createConnector(scheme + "://[test." + lengthPrepend + "." + batchEnabled + "." + format + "]?format="
						+ format + "&batchingEnabled=" + batchEnabled + "&lengthPrepend=" + lengthPrepend
						+ "&limitSize=" + LIMIT + "&deleteOnStartup=true&maxBatchSize=1000&producerOnly=true");
		connector.start();

		var producer = (FileProducer) connector.getProducer().orElseThrow();

		var msg = Message.newDefault(Payload.newDefault(BValue.newDefault("hello")));

		var latch = new CountDownLatch(NUM_MESSAGES);

		for (int i = 0; i < NUM_MESSAGES; i++) {
			producer.sendWithAck(msg).always((s, r, e) -> latch.countDown());
		}

		latch.await();

		connector.stop();

		return producer.getEngine().getTotalSentBytes();
	}

	private void printPace(String name, int numMessages, long elapsed, long totalSentBytes) {
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println("Total sent bytes: " + df.format(totalSentBytes));
		System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
				+ "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s" + " with bandwidth of "
				+ df.format(1e9 * totalSentBytes / elapsed / 1024 / 1024) + "MB/s");
	}
}
