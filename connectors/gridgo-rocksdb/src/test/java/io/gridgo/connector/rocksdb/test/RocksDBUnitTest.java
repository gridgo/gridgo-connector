package io.gridgo.connector.rocksdb.test;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.rocksdb.RocksDBConstants;
import io.gridgo.framework.support.Message;

public class RocksDBUnitTest {

    private static final int NUM_WARMUP_MESSAGES = 1;

    private static final int NUM_MESSAGES = 1;

    @Test
    public void testReads() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector("rocksdb://testreads.bin");
        var producer = connector.getProducer().get();
        connector.start();

        var writeLatch = new CountDownLatch(NUM_MESSAGES);
        for (var i = 0; i < NUM_MESSAGES; i++) {
            var msg = Message.ofAny( //
                    BObject.of(RocksDBConstants.OPERATION, RocksDBConstants.OPERATION_SET), //
                    BObject.of("prop-" + i, i));
            producer.sendWithAck(msg).done(result -> writeLatch.countDown());
        }
        writeLatch.await();

        System.out.println("Writes done, start reading");

        var atomicRef = new AtomicReference<Exception>();

        var readLatch = new CountDownLatch(NUM_MESSAGES);
        var start = System.nanoTime();
        for (var i = 0; i < NUM_MESSAGES; i++) {
            var msg = Message.ofAny( //
                    BObject.of(RocksDBConstants.OPERATION, RocksDBConstants.OPERATION_GET), //
                    "prop-" + i);
            var idx = i;
            producer.call(msg).done(result -> {
                try {
                    var body = result.body();
                    Assert.assertEquals((Integer) idx, body.asValue().getInteger());
                } catch (Exception ex) {
                    atomicRef.set(ex);
                }
            }) //
                    .fail(atomicRef::set) //
                    .always((s, r, e) -> readLatch.countDown());
        }
        readLatch.await();
        var elapsed = System.nanoTime() - start;
        printPace("RocksDB reads", NUM_MESSAGES, elapsed);

        connector.stop();

        if (atomicRef.get() != null)
            atomicRef.get().printStackTrace();

        Assert.assertNull(atomicRef.get());
    }

    @Test
    public void testWrites() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector("rocksdb://testwrites.bin");
        var producer = connector.getProducer().get();
        connector.start();

        System.out.println("Warming up...");

        var warmUpLatch = new CountDownLatch(NUM_WARMUP_MESSAGES);
        var testMsg = Message.ofAny( //
                BObject.of(RocksDBConstants.OPERATION, RocksDBConstants.OPERATION_SET), //
                BObject.of("0", 0));
        for (int i = 0; i < NUM_WARMUP_MESSAGES; i++) {
            producer.sendWithAck(testMsg) //
                    .done(result -> warmUpLatch.countDown());
        }
        warmUpLatch.await();

        System.out.println("Warmup done");

        var startSend = System.nanoTime();
        for (var i = 0; i < NUM_MESSAGES; i++) {
            var msg = Message.ofAny( //
                    BObject.of(RocksDBConstants.OPERATION, RocksDBConstants.OPERATION_SET), //
                    BObject.of("prop-" + i, i));
            producer.send(msg);
        }
        var elapsedSend = System.nanoTime() - startSend;
        printPace("RocksDB writes and forget", NUM_MESSAGES, elapsedSend);

        var cdl = new CountDownLatch(NUM_MESSAGES);
        var startAck = System.nanoTime();
        for (var i = 0; i < NUM_MESSAGES; i++) {
            var msg = Message.ofAny( //
                    BObject.of(RocksDBConstants.OPERATION, RocksDBConstants.OPERATION_SET), //
                    BObject.of("prop-" + i, i));
            producer.sendWithAck(msg).done(result -> cdl.countDown());
        }
        cdl.await();
        var elapsedAck = System.nanoTime() - startAck;
        printPace("RocksDB writes with ack", NUM_MESSAGES, elapsedAck);

        connector.stop();
    }

    private void printPace(String name, int numMessages, long elapsed) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        System.out.println(String.format("%s: %d operations were processed in %sms -> pace: %s", name, numMessages,
                df.format(elapsed / 1e6), df.format(1e9 * numMessages / elapsed) + "ops/s"));
    }
}
