package io.gridgo.redis.command;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;

public interface RedisConnectionCommands {

    public String auth(String password);

    public Promise<BElement, Exception> echo(byte[] msg);

    public Promise<BElement, Exception> ping();

    public Promise<BElement, Exception> quit();

    public String select(int db);

    public Promise<BElement, Exception> swapdb(int db1, int db2);

}
