package io.gridgo.connector.redis;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "redis", syntax = "{host}:{port}")
public class RedisConnector extends AbstractConnector {

	@Override
	protected void onInit() {
		var host = getPlaceholder("host");
		var port = getPlaceholder("port");
		
		this.producer = Optional.of(new RedisProducer(getContext(), host, port));
	}

}
