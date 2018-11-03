package io.gridgo.connector;

import java.util.Map;

public interface ConnectorConfig {

	public String getRemaining();
	
	public Map<String, Object> getParameters();
}
