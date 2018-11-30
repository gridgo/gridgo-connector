package io.gridgo.connector.redis.adapter;

import io.gridgo.utils.support.HostAndPortSet;
import lombok.Data;

@Data
public class RedisConfig {

	private RedisServerType serverType;
	private HostAndPortSet address;
	private String masterName;
	private String password;
	private int databaseNumber;
	private int timeout;
}
