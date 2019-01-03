package io.gridgo.connector.redis.impl;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleFailurePromise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.redis.RedisProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.RedisCommandHandler;
import io.gridgo.redis.command.RedisCommands;
import io.gridgo.redis.exception.CommandHandlerNotRegisteredException;

public class AbstractRedisProducer extends AbstractProducer implements RedisProducer {

    private final RedisClient redisClient;

    protected AbstractRedisProducer(ConnectorContext context, RedisClient redisClient) {
        super(context);
        this.redisClient = redisClient;
    }

    @Override
    public Promise<Message, Exception> call(Message request) {

        BObject headers = request.getPayload().getHeaders();

        String command = headers.getString("command", headers.getString("cmd", null));
        RedisCommandHandler handler = RedisCommands.getHandler(command);

        if (handler == null)
            return new SimpleFailurePromise<>(new CommandHandlerNotRegisteredException("Handler doesn't registered for command: " + command));

        Promise<BElement, Exception> promise = handler.execute(redisClient, headers, request.getPayload().getBody());

        return promise.filterDone(result -> {
            return Message.ofAny(result);
        });
    }

    @Override
    protected String generateName() {
        return "producer.redis." + redisClient.getName();
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    @Override
    protected void onStart() {
        // do nothing...
    }

    @Override
    protected void onStop() {
        // do nothing...
    }

    @Override
    public void send(Message message) {
        call(message);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return call(message);
    }
}
