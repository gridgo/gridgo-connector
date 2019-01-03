package io.gridgo.connector.redis.test;

import org.junit.Test;

import lombok.AccessLevel;
import lombok.Getter;

public class SingleRedisUnitTest extends RedisUnitTest {

    @Getter(AccessLevel.PROTECTED)
    private final String endpoint = "redis:single://[localhost:6379]";

    @Test
    @Override
    public void testAppend() throws InterruptedException {
        super.testAppend();
    }

    @Test
    @Override
    public void testSetAndGet() throws InterruptedException {
        super.testSetAndGet();
    }
}
