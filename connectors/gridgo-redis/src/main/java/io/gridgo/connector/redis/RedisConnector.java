package io.gridgo.connector.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.exceptions.InvalidParamException;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.RedisClientFactory;
import io.gridgo.redis.RedisConfig;
import io.gridgo.redis.RedisType;
import io.gridgo.utils.support.HostAndPortSet;

@ConnectorEndpoint(scheme = "redis", syntax = "{type}://{address}")
public class RedisConnector extends AbstractConnector {

    private RedisClient redisClient;
    private Collection<String> topics;

    @Override
    protected void onInit() {
        var type = RedisType.forName(this.getPlaceholder("type"));
        if (type == null) {
            throw new InvalidPlaceholderException(
                    "Redis type should be one of: SINGLE, SENTINEL, MASTER_SLAVE (or masterSlave also) and CLUSTER (case insensitive), got "
                            + this.getPlaceholder("type"));
        }

        var addrString = this.getPlaceholder("address");
        if (addrString == null || addrString.isBlank()) {
            throw new InvalidPlaceholderException("Address should be specified and cannot be blank");
        }

        var address = new HostAndPortSet(addrString);

        var configBuilder = RedisConfig.builder().address(address);

        if (type == RedisType.SENTINEL) {
            var sentinelMasterId = this.getParam("sentinelMasterId");
            if (sentinelMasterId != null && !sentinelMasterId.isBlank()) {
                configBuilder.sentinelMasterId(sentinelMasterId);
            } else {
                throw new InvalidParamException("Redis Sentinel require param sentinelMasterId");
            }
        }

        var topicsParam = this.getParam("topics", null);
        var topics = topicsParam == null ? null : topicsParam.split(",");
        if (topics != null) {
            this.topics = new ArrayList<>();
            for (String topic : topics) {
                this.topics.add(topic.trim());
            }
        }

        this.redisClient = RedisClientFactory.newDefault().newClient(type, configBuilder.build());

        if (this.topics != null)
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
