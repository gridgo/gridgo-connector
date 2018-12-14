package io.gridgo.connector.redis;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.redis.adapter.RedisClient;
import io.gridgo.connector.redis.adapter.RedisClientFactory;
import io.gridgo.connector.redis.adapter.RedisConfig;
import io.gridgo.connector.redis.adapter.RedisConfig.RedisConfigBuilder;
import io.gridgo.connector.redis.adapter.RedisType;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.utils.support.HostAndPortSet;

@ConnectorEndpoint(scheme = "redis", syntax = "{type}://{address}")
public class RedisConnector extends AbstractConnector {

    private RedisClient redisClient;

    @Override
    protected void onInit() {
        RedisType type = RedisType.forName(this.getPlaceholder("type"));
        if (type == null) {
            throw new InvalidPlaceholderException(
                    "Redis type should be one of: SINGLE, MASTER_SLAVE (or masterSlave also) and CLUSTER (case insensitive), got "
                            + this.getPlaceholder("type"));
        }

        String addrString = this.getPlaceholder("address");
        if (addrString == null || addrString.isBlank()) {
            throw new InvalidPlaceholderException("Address should be specified and cannot be blank");
        }

        HostAndPortSet address = new HostAndPortSet(addrString);

        RedisConfigBuilder configBuilder = RedisConfig.builder().address(address);

        if (type == RedisType.MASTER_SLAVE) {
            String sentinelAddrString = this.getParam("sentinel");
            if (sentinelAddrString != null && !sentinelAddrString.isBlank()) {
                configBuilder.sentinelAddress(new HostAndPortSet(sentinelAddrString));
            }
        }

        this.redisClient = RedisClientFactory.newDefault().newClient(type, configBuilder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.redisClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.redisClient.stop();
    }
}
