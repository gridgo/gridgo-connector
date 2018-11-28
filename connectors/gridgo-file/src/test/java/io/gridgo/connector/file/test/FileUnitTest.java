package io.gridgo.connector.file.test;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.connector.file.FileProducer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class FileUnitTest {

	private static final int NUM_MESSAGES = 1;
	private static final int BYTE_SIZE = 1;

	@Test
	public void testBatchNoLengthPrepend() throws InterruptedException {
		System.out.println("Test batching without length prepend\n");
		doTestFile("file:disruptor", "xml", "true", "false");
		doTestFile("file:disruptor", "json", "true", "false");
		doTestFile("file:disruptor", "raw", "true", "false");
		System.out.println("\n-----\n");
	}

	@Test
	public void testBatchWithLengthPrepend() throws InterruptedException {
		System.out.println("Test batching with length prepend\n");
		doTestFile("file:disruptor", "xml", "true", "true");
		doTestFile("file:disruptor", "json", "true", "true");
		doTestFile("file:disruptor", "raw", "true", "true");
		System.out.println("-----");
	}

	@Test
	public void testNonBatch() throws InterruptedException {
		doTestFile("file:disruptor", "xml", "false", "false");
		doTestFile("file:disruptor", "json", "false", "false");
		doTestFile("file:disruptor", "raw", "false", "false");
	}

	@Test
	public void testBasic() throws InterruptedException {
		doTestFile("file", "xml", "false", "false");
		doTestFile("file", "json", "false", "false");
		doTestFile("file", "raw", "false", "false");
	}

	private void doTestFile(String scheme, String format, String batchEnabled, String lengthPrepend)
			throws InterruptedException {
		var connector = new DefaultConnectorFactory().createConnector(
				scheme + "://[test.txt]?format=" + format + "&batchingEnabled=" + batchEnabled + "&lengthPrepend="
						+ lengthPrepend + "&deleteOnStartup=true&deleteOnShutdown=true&maxBatchSize=1000");
		connector.start();

		var producer = (FileProducer) connector.getProducer().orElseThrow();

		var randomBytes = new byte[BYTE_SIZE];
		new Random().nextBytes(randomBytes);

		var msg = Message.newDefault(Payload.newDefault(BValue.newDefault(randomBytes)));

		var warmUpLatch = new CountDownLatch(1);
		producer.sendWithAck(msg).always((s, r, e) -> warmUpLatch.countDown());
		warmUpLatch.await();

		var latch = new CountDownLatch(NUM_MESSAGES);

		var start = System.nanoTime();
		for (int i = 0; i < NUM_MESSAGES; i++) {
			producer.sendWithAck(msg).always((s, r, e) -> latch.countDown());
		}

		latch.await();

		var elapsed = System.nanoTime() - start;
		printPace(scheme + " (format=" + format + ", batchingEnabled=" + batchEnabled + ", lengthPrepend="
				+ lengthPrepend + ")\n", NUM_MESSAGES, elapsed, producer.getEngine().getTotalSentBytes());

		connector.stop();
	}

	private void printPace(String name, int numMessages, long elapsed, long totalSentBytes) {
		DecimalFormat df = new DecimalFormat("###,###.##");
		System.out.println(name + ": " + numMessages + " operations were processed in " + df.format(elapsed / 1e6)
				+ "ms -> pace: " + df.format(1e9 * numMessages / elapsed) + "ops/s" + " with bandwidth of "
				+ df.format(1e9 * totalSentBytes / elapsed / 1024 / 1024) + "MB/s");
	}
}
