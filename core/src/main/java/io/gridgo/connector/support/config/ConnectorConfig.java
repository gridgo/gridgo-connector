package io.gridgo.connector.support.config;

import java.util.Map;
import java.util.Properties;

public interface ConnectorConfig {

	public String getRemaining();
	
	public Map<String, Object> getParameters();
	
	public Properties getPlaceholders();
}
