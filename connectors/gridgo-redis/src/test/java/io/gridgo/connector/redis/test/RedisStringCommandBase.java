package io.gridgo.connector.redis.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import io.gridgo.redis.command.string.RedisMGetHandler;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
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

    /*
     * https://redis.io/commands/decr
     */
    public void testDecrementCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "10")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DECR), "mykey")))//
                .pipeDone(result -> Common.checkLongResult(result, 9))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "234293482390480948029348230948"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DECR), "mykey")))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    if (ex.getMessage().equals("io.lettuce.core.RedisCommandExecutionException: ERR value is not an integer or out of range")) {
                        exRef.set(null);
                        latch.countDown();
                        return;
                    }
                    exRef.set(ex);
                    latch.countDown();
                });


        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /**
     * https://redis.io/commands/decrby
     */
    public void testDecrbyCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "10")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DECRBY), BArray.ofSequence("mykey", 3))))//
                .pipeDone(result -> Common.checkLongResult(result, 7))//
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
     * https://redis.io/commands/mset
     */
    public void testMsetCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.MSET), BArray.ofSequence("key1", "Hello", "key2", "World")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "key1")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "key2")))//
                .pipeDone(result -> Common.checkStringResult(result, "World"))//
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
     * https://redis.io/commands/getset
     */
    public void testGetsetCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.MSET), BArray.ofSequence("key1", "Hello", "key2", "World")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "key1")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "key2")))//
                .pipeDone(result -> Common.checkStringResult(result, "World"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /**
     * https://redis.io/commands/mget
     */
    public void testMGetCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "Hello")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "World"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.MGET), BArray.ofSequence("key1", "key2", "nonexistkey"))))//
                .pipeDone(result -> {
                    System.out.println(result.getPayload().getHeaders());
                    System.out.println(result);
                    var responses = result.body().asArray();
                    System.out.println("responses" + responses);
                    if ("Hello".equals(responses.get(0).asArray().get(1).toString())
                            && "World".equals(responses.get(1).asArray().get(1).toString())) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new RuntimeException());

                })//
                .done(result -> {
                    System.out.println("res: " + result);
                    latch.countDown();
                })//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });


        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    /*
     * https://redis.io/commands/getrange
     */
    public void testGetRangeCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "This is a string")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETRANGE), BArray.ofSequence("mykey", 0, 3))))//
                .pipeDone(result -> Common.checkStringResult(result, "This"))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETRANGE), BArray.ofSequence("mykey", -3, -1))))//
                .pipeDone(result -> Common.checkStringResult(result, "ing"))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETRANGE), BArray.ofSequence("mykey", 0, -1))))//
                .pipeDone(result -> Common.checkStringResult(result, "This is a string"))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GETRANGE), BArray.ofSequence("mykey", 10, 100))))//
                .pipeDone(result -> Common.checkStringResult(result, "string"))
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());

    }

    public void testStrLenCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "Hello world")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.STRLEN), "mykey")))//
                .pipeDone(result -> Common.checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.STRLEN), "nonexisting")))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });
        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    public void testIncrCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.INCR), "mykey")))//
                .pipeDone(result -> Common.checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkStringResult(result, "11"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    public void testSetRangeCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "Hello World")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETRANGE), BArray.ofSequence("key1", 6, "Redis"))))//
                .pipeDone(result -> Common.checkLongResult(result, 11))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "key1")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello Redis"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    protected void testSetNxCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETNX), BArray.ofSequence("mykey", "Hello"))))//
                .pipeDone(result -> Common.checkLongResult(result, 1))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETNX), BArray.ofSequence("mykey", "World"))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());

    }

    protected void testSetExCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        int hold = 10;
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SETEX), BArray.ofSequence("mykey", hold, "Hello"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.TTL), "mykey")))//
                .pipeDone(result -> {
                    if (result.body().asValue().getLong() > 0) {
                        return Promise.of(result);
                    }
                    return Promise.ofCause(new TimeoutException());
                })//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello"))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    protected void testIncrByCommand() throws InterruptedException {

        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);
        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.INCRBY), BArray.ofSequence("mykey", 10))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkLongResult(result, 20))//
                .done(result -> latch.countDown())//
                .fail(ex -> {
                    exRef.set(ex);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    protected void testMsetNxCommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.MSETNX), BArray.ofSequence("key1", "Hello", "key2", "there")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.MSETNX), BArray.ofSequence("key2", "there", "key3", "world"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.MGET), BArray.ofSequence("key1", "key2", "key3"))))//
                .pipeDone(result -> {
                    System.out.println("----DEBUG----");
                    System.out.println(result);
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

    protected void testPSetxECommand() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.PSETEX), BArray.ofSequence("mykey", 1000, "Hello")))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> Common.checkStringResult(result, "Hello"))//
                .pipeDone(result -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Promise.of(result);
                })//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> {
                    if (!result.body().asValue().isNull()) {
                        return Promise.ofCause(new RuntimeException());
                    }
                    return Promise.of(result);
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

    protected void testIncrByFloatCommand() throws InterruptedException {

        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", 10.50))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.INCRBYFLOAT), BArray.ofSequence("mykey", 0.1))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> {
                    var floatVal = result.body().asValue().getFloat();
                    if (floatVal != 10.6) {
                        return Promise.ofCause(new RuntimeException());
                    }
                    return Promise.of(result);
                })//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.INCRBYFLOAT), BArray.ofSequence("mykey", -5))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "mykey")))//
                .pipeDone(result -> {
                    var floatVal = result.body().asValue().getFloat();
                    if (floatVal != 5.6) {
                        return Promise.ofCause(new RuntimeException());
                    }
                    return Promise.of(result);
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
}
