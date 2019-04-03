package io.gridgo.connector.redis.test;

import org.junit.Test;

import lombok.AccessLevel;
import lombok.Getter;

public class SingleRedisStringCommand extends RedisStringCommandBase {

    @Getter(AccessLevel.PROTECTED)
    private final String endpoint = "redis:single://[localhost:6379]";

    @Test
    @Override
    public void testBitcountCommand() throws InterruptedException {
        super.testBitcountCommand();
    }

    @Test
    @Override
    public void testBitopCommand() throws InterruptedException {
        super.testBitopCommand();
    }

    @Test
    @Override
    public void testBitposCommand() throws InterruptedException {
        super.testBitposCommand();
    }

    @Test
    @Override
    public void testBitFieldCommand() throws InterruptedException {
        super.testBitFieldCommand();
    }

    @Test
    @Override
    public void testSetBitCommand() throws InterruptedException {
        super.testSetBitCommand();
    }

    @Test
    @Override
    public void testGBitCommand() throws InterruptedException {
        super.testGBitCommand();
    }

    @Test
    @Override
    public void testDecrementCommand() throws InterruptedException {
        super.testDecrementCommand();
    }

    @Test
    @Override
    public void testDecrbyCommand() throws InterruptedException {
        super.testDecrbyCommand();
    }

    @Test
    @Override
    public void testMsetCommand() throws InterruptedException {
        super.testMsetCommand();
    }

    @Test
    @Override
    public void testMGetCommand() throws InterruptedException {
        super.testMGetCommand();
    }

    @Test
    @Override
    public void testGetsetCommand() throws InterruptedException {
        super.testGetsetCommand();
    }

    @Test
    @Override
    public void testGetRangeCommand() throws InterruptedException {
        super.testGetRangeCommand();
    }

    @Test
    @Override
    public void testStrLenCommand() throws InterruptedException {
        super.testStrLenCommand();
    }

    @Test
    @Override
    public void testIncrCommand() throws InterruptedException {
        super.testIncrCommand();
    }

    @Test
    @Override
    public void testSetRangeCommand() throws InterruptedException {
        super.testSetRangeCommand();
    }

    @Test
    @Override
    public void testSetNxCommand() throws InterruptedException {
        super.testSetNxCommand();
    }

    @Test
    @Override
    public void testSetExCommand() throws InterruptedException {
        super.testSetExCommand();
    }

    @Test
    @Override
    public void testIncrByCommand() throws InterruptedException {
        super.testIncrByCommand();
    }

    @Test
    @Override
    public void testMsetNxCommand() throws InterruptedException {
        super.testMsetNxCommand();
    }

    @Test
    @Override
    public void testPSetxECommand() throws InterruptedException {
        super.testPSetxECommand();
    }

    @Test
    @Override
    public void testIncrByFloatCommand() throws InterruptedException {
        super.testIncrByFloatCommand();
    }

    @Test
    @Override
    public void testEcho() throws InterruptedException {
        super.testEcho();
    }

    @Test
    @Override
    public void testDelete() throws InterruptedException {
        super.testDelete();
    }

    @Test
    @Override
    public void testSadd() throws InterruptedException {
        super.testSadd();
    }

    @Test
    @Override
    public void testScard() throws InterruptedException {
        super.testScard();
    }

    @Test
    @Override
    public void testSdiff() throws InterruptedException {
        super.testSdiff();
    }

    @Test
    @Override
    public void testSdiffStore() throws InterruptedException {
        super.testSdiffStore();
    }

    @Test
    @Override
    public void testSinter() throws InterruptedException {
        super.testSinter();
    }

    @Test
    @Override
    public void testSinterStore() throws InterruptedException {
        super.testSinterStore();
    }

    @Test
    @Override
    public void testSismember() throws InterruptedException {
        super.testSismember();
    }

    @Test
    @Override
    public void testSmembers() throws InterruptedException {
        super.testSmembers();
    }

    @Test
    @Override
    public void testSmove() throws InterruptedException {
        super.testSmove();
    }

    @Test
    @Override
    public void testSpop() throws InterruptedException {
        super.testSpop();
    }

    @Test
    @Override
    public void testSrandMember() throws InterruptedException {
        super.testSrandMember();
    }

    @Test
    @Override
    public void testSrem() throws InterruptedException {
        super.testSrem();
    }

    @Test
    @Override
    public void testSunion() throws InterruptedException {
        super.testSunion();
    }

    @Test
    @Override
    public void testSunionStore() throws InterruptedException {
        super.testSunionStore();
    }
}
