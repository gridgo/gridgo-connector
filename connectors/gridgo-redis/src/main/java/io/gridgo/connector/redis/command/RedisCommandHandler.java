package io.gridgo.connector.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.redis.adapter.RedisClient;

public interface RedisCommandHandler {

    public String[] getKeyOrder();

    public Promise<BElement, Exception> execute(RedisClient redisClient, BElement params);
}
