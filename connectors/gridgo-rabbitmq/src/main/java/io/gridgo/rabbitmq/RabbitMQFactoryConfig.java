package io.gridgo.rabbitmq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RabbitMQFactoryConfig {

	private boolean automaticRecoveryEnabled;
	private int channelRpcTimeout;
	private boolean channelShouldCheckRpcResponseType;
}
