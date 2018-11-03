package io.gridgo.connector.impl.resolvers;

import java.util.Map;

import io.gridgo.connector.support.config.ConnectorConfig;
import lombok.Getter;

@Getter
public class DefaultConnectorConfig implements ConnectorConfig {
	
	private String remaining;

	private Map<String, Object> parameters;

	public DefaultConnectorConfig(String remaining, Map<String, Object> parameters) {
		this.remaining = remaining;
		this.parameters = parameters;
	}
}
