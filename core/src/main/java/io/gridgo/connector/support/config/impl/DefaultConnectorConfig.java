package io.gridgo.connector.support.config.impl;

import java.util.Map;
import java.util.Properties;

import io.gridgo.connector.support.config.ConnectorConfig;
import lombok.Getter;

@Getter
public class DefaultConnectorConfig implements ConnectorConfig {
	
	private String remaining;

	private Map<String, Object> parameters;
	
	private Properties placeholders;

	public DefaultConnectorConfig(String remaining, Map<String, Object> parameters, Properties placeholders) {
		this.remaining = remaining;
		this.parameters = parameters;
		this.placeholders = placeholders;
	}
}