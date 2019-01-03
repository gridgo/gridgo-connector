package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;

public interface RedisCommandHandler {

    public Promise<BElement, Exception> execute(RedisClient redisClient, BObject options, BElement arguments);

    public String[] getKeyOrder();
}
