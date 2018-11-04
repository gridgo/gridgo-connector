package io.gridgo.connector.support.config;

import java.util.Map;

public interface ConnectorConfig {

	public String getRemaining();
	
	public Map<String, Object> getParameters();
}
