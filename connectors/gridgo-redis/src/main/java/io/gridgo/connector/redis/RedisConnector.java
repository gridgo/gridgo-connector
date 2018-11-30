package io.gridgo.connector.redis;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.jedis.JedisClientFactory;
import io.gridgo.utils.support.HostAndPortSet;

@ConnectorEndpoint(scheme = "redis", syntax = "{mode}://[{password}@]{address}/[{databaseNumber}][#{master}]")
public class RedisConnector extends AbstractConnector {

	@Override
	protected void onInit() {
		RedisConfig config = extractConfig();
		RedisClient jedisService = new JedisClientFactory().getRedisClient(config);

		this.producer = Optional.of(new RedisProducer(getContext(), jedisService));
	}

	private RedisConfig extractConfig() {
		var mode = getPlaceholder("mode");
		var password = getPlaceholder("password");
		var databaseNumber = getPlaceholder("databaseNumber");

		HostAndPortSet address = new HostAndPortSet(getPlaceholder("address"));
		if (address.isEmpty()) {
			throw new InvalidPlaceholderException("Redis address(es) must be provided");
		}

		RedisConfig config = new RedisConfig();
		config.setServerType(RedisServerType.forName(mode));
		config.setAddress(address);
		config.setPassword(password);
		config.setDatabaseNumber(Integer.parseInt(databaseNumber));

		return config;
	}
}
