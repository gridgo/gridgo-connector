package io.gridgo.connector.redis.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.joo.promise4j.Promise;
import org.junit.Assert;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.redis.command.RedisCommands;

public abstract class RedisStringCommandBase {

    private static final String CMD = "cmd";

    private BObject buildCommand(String command) {
        return BObject.of(CMD, command);
    }

    protected abstract String getEndpoint();

    /*
     * Test `bitcount` command https://redis.io/commands/bitcount
     */
    public void testBitcountCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "foobar")))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITCOUNT), BArray.ofSequence("mykey", "0", "0"))))//
                .pipeDone(result -> Common.checkLongResult(result, 4))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.BITCOUNT), BArray.ofSequence("mykey", 1, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 6)) //
                .done(msg -> latch.countDown()) //
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/bitop
     */

    public void testBitopCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "foobar"))) //
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITOP),
                        BArray.ofSequence(Const.BITOPAND, "resultAnd", BArray.ofSequence("key1", "key2")))))// case AND
                .pipeDone(result -> Common.checkLongResult(result, 6))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultAnd"))) //
                .pipeDone(result -> Common.checkStringResult(result, "`bc`ab"))

                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITOP),
                        BArray.ofSequence(Const.BITOPOR, "resultOr", BArray.ofSequence("key1", "key2")))))// case OR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultOr")))//
                .pipeDone(result -> Common.checkStringResult(result, "goofev"))

                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITOP),
                        BArray.ofSequence(Const.BITOPXOR, "resultXor", BArray.ofSequence("key1", "key2")))))// case XOR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultXor")))//
                .pipeDone(result -> Common.checkStringResult(result, "\\a\\r\\x0c\\x06\\x04\\x14"))

                .pipeDone(result -> producer
                        .call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer
                        .call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITOP),
                        BArray.ofSequence(Const.BITOPNOT, "dest", "key1"))))// case NOT
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "dest")))//
                .pipeDone(result -> Common.checkStringResult(result, "\\x99\\x90\\x90\\x9d\\x9e\\x8d"))

                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());

    }

    /*
     * https://redis.io/commands/bitpos
     */
    public void testBitposCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "foo"))) //
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", false))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true))))//
                .pipeDone(result -> Common.checkLongResult(result, 1))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 9))
                .pipeDone(result -> producer
                        .call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", false, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 8))
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true, 1, 2))))//
                .pipeDone(result -> Common.checkLongResult(result, 9))//
                .done(result -> latch.countDown()).fail(ex -> {
            exRef.set(ex);
            latch.countDown();
        });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/bitfield
     */

    public void testBitFieldCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "100")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITFIELD),
                        BArray.ofSequence("mykey", "INCRBY", "i5", "100", "1", "GET", "u4", "0"))))//
                .pipeDone(result -> {
                    var responses = result.body().asArray();
                    if (1L == responses.get(0).asValue().getLong() && 0L == responses.get(1).asValue().getLong()) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new RuntimeException());
                })//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }
    /*
     * https://redis.io/commands/setbit
     */

    public void testSetBitCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETBIT), BArray.ofSequence("mykey", 7, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETBIT), BArray.ofSequence("mykey", 7, 0))))//
                .pipeDone(result -> Common.checkLongResult(result, 1))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkStringResult(result, "\u0000"))//
                .done(result -> latch.countDown())
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/getbit
     */

    public void testGBitCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETBIT), BArray.ofSequence("mykey", 7, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETBIT), BArray.ofSequence("mykey", 0))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETBIT), BArray.ofSequence("mykey", 7))))//
                .pipeDone(result -> Common.checkLongResult(result, 1))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETBIT), BArray.ofSequence("mykey", 100))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .done(result -> latch.countDown())
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }
}
