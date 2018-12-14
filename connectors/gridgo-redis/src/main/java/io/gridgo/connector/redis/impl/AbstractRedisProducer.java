package io.gridgo.connector.redis.impl;

import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.redis.RedisProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;

public class AbstractRedisProducer extends AbstractProducer implements RedisProducer {

    protected AbstractRedisProducer(ConnectorContext context) {
        super(context);
    }

    @Override
    public void send(Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCallSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String generateName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub

    }

}
