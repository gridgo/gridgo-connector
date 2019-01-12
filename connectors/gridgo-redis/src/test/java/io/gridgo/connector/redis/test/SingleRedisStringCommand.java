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

}
