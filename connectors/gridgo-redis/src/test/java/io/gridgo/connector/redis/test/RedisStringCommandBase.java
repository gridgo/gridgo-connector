package io.gridgo.connector.redis.test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
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

    public abstract String getEndpoint();

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
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITCOUNT), BArray.ofSequence("mykey", "0", "0"))))//
                .pipeDone(result -> Common.checkLongResult(result, 4))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITCOUNT), BArray.ofSequence("mykey", 1, 1))))//
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
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITOP), BArray.ofSequence(Const.BITOPAND, "resultAnd", BArray.ofSequence("key1", "key2")))))// case
                                                                                                                                                             // AND
                .pipeDone(result -> Common.checkLongResult(result, 6))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultAnd"))) //
                .pipeDone(result -> Common.checkStringResult(result, "`bc`ab"))

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITOP), BArray.ofSequence(Const.BITOPOR, "resultOr", BArray.ofSequence("key1", "key2")))))// case
                                                                                                                                                           // OR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultOr")))//
                .pipeDone(result -> Common.checkStringResult(result, "goofev"))

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITOP), BArray.ofSequence(Const.BITOPXOR, "resultXor", BArray.ofSequence("key1", "key2")))))// case
                                                                                                                                                             // XOR
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), "resultXor")))//
                .pipeDone(result -> Common.checkStringResult(result, "\\a\\r\\x0c\\x06\\x04\\x14"))

                .pipeDone(result -> producer.call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key1", "foobar"))))//
                .pipeDone(result -> producer.call(Message.ofAny(BObject.of(CMD, "set"), BArray.ofSequence("key2", "abcdef"))))//

                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITOP), BArray.ofSequence(Const.BITOPNOT, "dest", "key1"))))// case
                                                                                                                                                       // NOT
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
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", false))))//
                .pipeDone(result -> Common.checkLongResult(result, 0))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true))))//
                .pipeDone(result -> Common.checkLongResult(result, 1))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 9))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", false, 1))))//
                .pipeDone(result -> Common.checkLongResult(result, 8))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.BITPOS), BArray.ofSequence("mykey", true, 1, 2))))//
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
                .pipeDone(result -> producer.call(
                        Message.ofAny(buildCommand(RedisCommands.BITFIELD), BArray.ofSequence("mykey", "INCRBY", "i5", "100", "1", "GET", "u4", "0"))))//
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
                .done(result -> latch.countDown()).fail(ex -> {
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
                .done(result -> latch.countDown()).fail(ex -> {
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
                    if ("Hello".equals(responses.get(0).asArray().get(1).toString()) && "World".equals(responses.get(1).asArray().get(1).toString())) {
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
                .pipeDone(result -> Common.checkStringResult(result, "string")).done(result -> latch.countDown())//
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

    public void testSetNxCommand() throws InterruptedException {
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

    public void testSetExCommand() throws InterruptedException {
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

    public void testIncrByCommand() throws InterruptedException {

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

    public void testMsetNxCommand() throws InterruptedException {
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

    public void testPSetxECommand() throws InterruptedException {
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

    public void testIncrByFloatCommand() throws InterruptedException {

        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "mykey"))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("mykey", "10.5"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.INCRBYFLOAT), BArray.ofSequence("mykey", 0.1f))))
                .done(result -> latch.countDown()).fail(failedCause -> {
                    exRef.set(failedCause);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    public void testEcho() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.ECHO), "hello world")).done(result -> {
            var body = result.body();
            if (!body.isValue() || !StringUtils.equals("hello world", body.asValue().convertToString().getString())) {
                exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testDelete() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("del", 10.5f)))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "del"))).done(result -> {
                    var body = result.body();
                    if (1 != body.asValue().getLong()) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    /**
     * List command Sets url: https://redis.io/commands#set
     * 
     * @throws InterruptedException
     */

    public void testSadd() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sadd", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sadd", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SMEMBERS), "sadd"))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testScard() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("scard", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("scard", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SMEMBERS), "scard")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SCARD), "scard"))).done(result -> {
                    var body = result.body();
                    if (body.asValue().getLong() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSdiff() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "e"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SDIFF), BArray.ofSequence("diff1", "diff2")))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSdiffStore() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("diff2", "e"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SDIFFSTORE), BArray.ofSequence("diff", "diff1", "diff2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSinter() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "e"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SINTER), BArray.ofSequence("sinter1", "sinter2")))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.toJson()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSinterStore() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "a")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "b"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter1", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "c"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "d"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sinter2", "e"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SINTERSTORE), BArray.ofSequence("sinter1", "sinter2"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSismember() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sismember", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SISMEMBER), BArray.ofSequence("sismember", "one")))).done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSmembers() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("smembers", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("smembers", "World"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SMEMBERS), "smembers"))).done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });
        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSmove() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("smove1", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("smove1", "two"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("smove2", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SMOVE), BArray.ofSequence("smove1", "smove2", "two"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 1) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSpop() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("spop", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("spop", "two"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("spop", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SPOP), BArray.ofSequence("spop")))).done(result -> {
                    var body = result.body();
                    if (StringUtils.isEmpty(body.asValue().getString())) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSrandMember() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("srandmember", "one", "two", "three")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SRANDMEMBER), BArray.ofSequence("srandmember", "2"))))
                .done(result -> {

                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSrem() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("srem", "one", "two", "three")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SRANDMEMBER), BArray.ofSequence("srem", "one")))).done(result -> {
//                    var body = result.body();
//                    if(body.asArray().size() != 1) {
//                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
//                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSunion() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion1", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion2", "two"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion3", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SUNION), BArray.ofSequence("sunion1", "sunion2", "sunion3"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asArray().size() != 3) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testSunionStore() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion1", "one")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion2", "two"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SADD), BArray.ofSequence("sunion3", "three"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SUNIONSTORE), BArray.ofSequence("sunion1", "sunion2", "sunion3"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getInteger() != 2) {
                        exRef.set(new RuntimeException("Body mismatch: " + body.asValue().getString()));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testScan() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SCAN), 0)).done(result -> {
            var body = result.body();
            if (body.asObject().getArray("keys") == null) {
                exRef.set(new RuntimeException("Body mismatch"));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    /**
     * GEO redis url: https://redis.io/commands#geo
     */
    public void testGeoAdd() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "Sicily")).pipeDone(
                result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GEOADD), BArray.ofSequence("Sicily", "-73.9454966", "40.747533", "Palermo"))))
                .done(result -> {
                    var body = result.body();
                    if (body.asValue().getLong() != 1L) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());

    }

    /**
     * Key command url: https://redis.io/commands#generic
     */
    public void testDel() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.APPEND), BArray.ofSequence("del", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DEL), "del"))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("result must equal to 1"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testDump() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.APPEND), BArray.ofSequence("dump", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.DUMP), "dump"))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (body.length() == 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testExists() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("set", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXISTS), "set"))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testExpire() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("expire", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXPIRE), BArray.ofSequence("expire", 10)))).pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.TTL), "expire"));
                }).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 10) {
                        exRef.set(new RuntimeException("Result must equal to 10"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testExpireat() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        long currentTimestamp = Instant.now().toEpochMilli();
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("expireat", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXPIREAT), BArray.ofSequence("expireat", currentTimestamp + 1000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.TTL), "expireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body > currentTimestamp) {
                        exRef.set(new RuntimeException("Result must greater than " + currentTimestamp));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testKeys() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("KEY1", "1");
        map.put("KEY2", "2");
        map.put("KEY3", "3");

        producer.call(Message.ofAny(buildCommand(RedisCommands.MSET), BArray.ofSequence(map)))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.KEYS), "KEY*"))).done(result -> {
                    var body = result.body().asArray();
                    Predicate<String> predicate = value -> map.containsKey(value);
                    body.stream().forEach(bElement -> {
                        if (!predicate.test(bElement.asValue().getString())) {
                            exRef.set(new RuntimeException("Body miss match"));
                        }
                    });
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    public void testPersits() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("persits", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXPIRE), BArray.ofSequence("persits", 10)))).pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.PERSIST), "persits"));
                }).done(result -> {
                    var body = result.body().asValue();
                    if (!body.getBoolean()) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();
        connector.stop();
        Assert.assertNull(exRef.get());
    }

    public void testPExpire() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        // long currentTimestamp = Instant.now().toEpochMilli();
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.PEXPIRE), BArray.ofSequence("pexpireat", 5000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body <= 0) {
                        exRef.set(new RuntimeException("Body mismtach"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testPExpireat() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        long currentTimestamp = Instant.now().toEpochMilli();
        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXPIREAT), BArray.ofSequence("pexpireat", currentTimestamp + 5000))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body < currentTimestamp) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testPTTL() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("pexpireat", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.EXPIRE), BArray.ofSequence("pexpireat", 1)))).pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Result must equal to 1"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.PTTL), "pexpireat"));
                }).done(result -> {
                    var body = result.body().asValue().getLong();
                    if (body < 0 || body > 1000) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testRandomkey() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.RANDOMKEY), null)).done(result -> {
            var body = result.body().asValue().getString();
            if (StringUtils.isEmpty(body)) {
                exRef.set(new RuntimeException("Body mismatch"));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testRename() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("rename", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.RENAME), BArray.ofSequence("rename", "rename1"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.GET), BArray.ofSequence("rename1")))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (StringUtils.isEmpty(body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testRenamenx() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("renamenx1", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("renamenx2", "WORLD"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.RENAMENX), BArray.ofSequence("renamenx1", "renamenx2"))))
                .pipeDone(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    return producer.call(Message.ofAny(buildCommand(RedisCommands.GET), BArray.ofSequence("renamenx2")));
                }).done(result -> {
                    var body = result.body().asValue().getString();
                    if (StringUtils.isEmpty(body) || !StringUtils.equals("WORLD", body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testTouch() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("touch1", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("touch2", "WORLD"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.TOUCH), BArray.ofSequence("touch1", "touch2")))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testType() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("type", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.TYPE), "type"))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "string")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testUnlink() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("unlink1", "Hello")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.SET), BArray.ofSequence("unlink2", "WORLD"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.TOUCH), BArray.ofSequence("unlink1", "unlink2")))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 2) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    /**
     * Test Hash url: https://redis.io/commands#hash
     */
    public void testHash() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hash", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HDEL), BArray.ofSequence("hash", "field1")))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHashExist() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hashexist", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HEXISTS), BArray.ofSequence("hashexist", "field1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 1) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHashGet() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hashget", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HGET), BArray.ofSequence("hashget", "field1")))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "foo")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHashGetAll() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hashgetall", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hashgetall", "field2", "bar"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("hashgetall", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HGETALL), BArray.ofSequence("hashgetall")))).done(result -> {
                    var body = result.body().asObject();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHINCRBY() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HINCRBY", "field1", "5")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HINCRBY), BArray.ofSequence("HINCRBY", "field1", "1"))))
                .done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 6) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHINCRBYFLOAT() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HINCRBYFLOAT", "field1", "5")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HINCRBYFLOAT), BArray.ofSequence("HINCRBYFLOAT", "field1", 0.1))))
                .done(result -> {
                    var body = result.body().asValue().getDouble();
                    if (body <= 5) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHKEYS() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HKEYS", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HKEYS", "field2", "bar"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HKEYS", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HKEYS), BArray.ofSequence("HKEYS")))).done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHLEN() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HLEN", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HLEN", "field2", "bar"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HLEN", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HLEN), BArray.ofSequence("HLEN")))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 3) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHMGET() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field2", "bar"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HMGET), BArray.ofSequence("HMGET", "field1", "field2", "field3"))))
                .done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHMSET() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HMSET), BArray.ofSequence("HMSET", "field1", "foo", "field2", "bar"))).done(result -> {
            var body = result.body().asValue().getString();
            if (!StringUtils.equals("OK", body)) {
                exRef.set(new RuntimeException("Body mismatch"));
            }
            latch.countDown();
        }).fail(e -> {
            exRef.set(e);
            latch.countDown();
        });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHSET() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HSET", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HGET), BArray.ofSequence("HSET", "field1")))).done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals("foo", body)) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHSETNX() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HSETNX", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HGET), BArray.ofSequence("HSETNX", "field1", "bar"))))
                .done(result -> {
                    var body = result.body().asValue().getString();
                    if (!StringUtils.equals(body, "foo")) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHSTRLEN() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HMSET), BArray.ofSequence("HSTRLEN", "field1", "HelloWorld", "field2", "bar")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSTRLEN), BArray.ofSequence("HSTRLEN", "field1")))).done(result -> {
                    var body = result.body().asValue().getInteger();
                    if (body != 10) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }

    public void testHVALS() throws InterruptedException {
        var connector = new DefaultConnectorFactory().createConnector(this.getEndpoint());
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        var exRef = new AtomicReference<Exception>();
        var latch = new CountDownLatch(1);

        producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field1", "foo")))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field2", "bar"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HSET), BArray.ofSequence("HMGET", "field3", "sekiro"))))
                .pipeDone(result -> producer.call(Message.ofAny(buildCommand(RedisCommands.HVALS), BArray.ofSequence("HMGET")))).done(result -> {
                    var body = result.body().asArray();
                    if (body.size() <= 0) {
                        exRef.set(new RuntimeException("Body mismatch"));
                    }
                    latch.countDown();
                }).fail(e -> {
                    exRef.set(e);
                    latch.countDown();
                });

        latch.await();

        connector.stop();

        Assert.assertNull(exRef.get());
    }
}
