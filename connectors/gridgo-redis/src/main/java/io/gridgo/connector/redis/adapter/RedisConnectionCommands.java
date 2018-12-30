package io.gridgo.connector.redis.adapter;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisConnectionCommands {

    public String auth(String password);

    public String select(int db);

    public Promise<BElement, Exception> echo(byte[] msg);

    public Promise<BElement, Exception> ping();

    public Promise<BElement, Exception> quit();

    public Promise<BElement, Exception> swapdb(int db1, int db2);

}
