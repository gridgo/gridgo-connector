package io.gridgo.connector.redis.impl;

import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.redis.RedisConsumer;
import io.gridgo.connector.support.config.ConnectorContext;

public class AbstractRedisConsumer extends AbstractConsumer implements RedisConsumer {

    protected AbstractRedisConsumer(ConnectorContext context) {
        super(context);
    }

    @Override
    protected String generateName() {
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
