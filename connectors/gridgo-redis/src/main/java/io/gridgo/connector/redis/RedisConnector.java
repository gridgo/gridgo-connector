package io.gridgo.connector.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisClientFactory;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisConfig.RedisConfigBuilder;
import io.gridgo.connector.redis.adapter.RedisType;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.exceptions.InvalidParamException;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.utils.support.HostAndPortSet;

@ConnectorEndpoint(scheme = "redis", syntax = "{type}://{address}")
public class RedisConnector extends AbstractConnector {

    private RedisClient redisClient;
    private Collection<String> topics;

    @Override
    protected void onInit() {
        RedisType type = RedisType.forName(this.getPlaceholder("type"));
        if (type == null) {
            throw new InvalidPlaceholderException("Redis type should be one of: SINGLE, MASTER_SLAVE (or masterSlave also) and CLUSTER (case insensitive), got "
                    + this.getPlaceholder("type"));
        }

        String addrString = this.getPlaceholder("address");
        if (addrString == null || addrString.isBlank()) {
            throw new InvalidPlaceholderException("Address should be specified and cannot be blank");
        }

        HostAndPortSet address = new HostAndPortSet(addrString);

        RedisConfigBuilder configBuilder = RedisConfig.builder().address(address);

        if (type == RedisType.SENTINEL) {
            String sentinelMasterId = this.getParam("sentinelMasterId");
            if (sentinelMasterId != null && !sentinelMasterId.isBlank()) {
                configBuilder.sentinelMasterId(sentinelMasterId);
            } else {
                throw new InvalidParamException("Redis Sentinel require param sentinelMasterId");
            }
        }

        String topicsParam = this.getParam("topics", null);
        String[] topics = topicsParam == null ? null : topicsParam.split(",");
        if (topics != null) {
            this.topics = new ArrayList<>();
            for (String topic : topics) {
                this.topics.add(topic.trim());
            }
        }

        this.redisClient = RedisClientFactory.newDefault().newClient(type, configBuilder.build());

        this.consumer = Optional.of(RedisConsumer.of(getContext(), redisClient, this.topics));
        this.producer = Optional.of(RedisProducer.of(getContext(), redisClient));
    }

    @Override
    protected void onStart() {
        this.redisClient.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        this.redisClient.stop();
        super.onStop();
    }
}
