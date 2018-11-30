package io.gridgo.connector.redis.adapter;

public enum RedisServerType {

	SINGLE, SENTINEL, CLUSTER;

	public static RedisServerType forName(String name) {
		if (name != null) {
			for (RedisServerType value : values()) {
				if (value.name().equalsIgnoreCase(name)) {
					return value;
				}
			}
		}
		return null;
	}
}
