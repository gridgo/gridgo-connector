package io.gridgo.connector.redis.impl;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.redis.RedisProducer;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.command.RedisCommandHandler;
import io.gridgo.connector.redis.command.RedisCommands;
import io.gridgo.connector.redis.exception.CommandHandlerNotRegisteredException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;

public class AbstractRedisProducer extends AbstractProducer implements RedisProducer {

    private final RedisClient redisClient;

    protected AbstractRedisProducer(ConnectorContext context, RedisClient redisClient) {
        super(context);
        this.redisClient = redisClient;
    }

    @Override
    protected String generateName() {
        return null;
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
    public boolean isCallSupported() {
        return true;
    }

    @Override
    public void send(Message message) {
        call(message);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return call(message);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        BObject headers = request.getPayload().getHeaders();
        String command = headers.getString("commands", headers.getString("cmd", null));
        RedisCommandHandler handler = RedisCommands.getHandler(command);
        if (handler != null) {
            Promise<BElement, Exception> promise = handler.execute(redisClient, request.getPayload().getBody());
            return promise.filterDone(result -> {
                return Message.ofAny(result);
            });
        }
        throw new CommandHandlerNotRegisteredException("Handler doesn't registered for command: " + command);
    }
}
