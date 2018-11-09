package io.gridgo.connector.rabbitmq;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.gridgo.bean.BObject;
import io.gridgo.connector.support.exceptions.InvalidParamException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RabbitMQQueueConfig {

	public static final Collection<String> EXCHANGE_TYPES = Arrays.asList("direct", "fanout", "topic", "headers");

	private String exchangeName = "";

	private String exchangeType = "direct";

	private String queueName = null;

	private boolean durable = false;

	private boolean exclusive = false;

	private boolean autoDelete = true;

	private boolean rpc = false;

	private final List<String> routingKeys = new LinkedList<>();

	public RabbitMQQueueConfig(BObject sourceConfig) {
		this.readFromBObject(sourceConfig);
	}

	/**
	 * 
	 * @return the queueName if exchangeName is blank, otherwise return the first
	 *         element found in list rountingKeys. If the list routingKeys is empty,
	 *         return null;
	 */
	public String getDefaultRoutingKey() {
		if (exchangeName.isBlank()) {
			return this.queueName;
		}
		return routingKeys.isEmpty() ? null : routingKeys.get(0);
	}

	public void readFromBObject(BObject sourceConfig) {
		this.exchangeName = sourceConfig.getString("exchangeName", this.exchangeName);
		if (this.exchangeName == null) {
			this.exchangeName = "";
		}
		this.exchangeName = this.exchangeName.trim();

		this.queueName = sourceConfig.getString("queueName", this.queueName);

		this.exchangeType = sourceConfig.getString("exchangeType", this.exchangeType);
		if (exchangeType == null || !EXCHANGE_TYPES.contains(exchangeType.trim())) {
			throw new InvalidParamException("Exchange type invalid, expect one of " + EXCHANGE_TYPES);
		}

		this.durable = sourceConfig.getBoolean("durable", this.durable);
		this.exclusive = sourceConfig.getBoolean("exclusive", this.exclusive);
		this.autoDelete = sourceConfig.getBoolean("autoDelete", this.autoDelete);
		this.rpc = sourceConfig.getBoolean("rpc", this.rpc);

		String routingKey = sourceConfig.getString("routingKey", null);
		if (routingKey != null) {
			this.routingKeys.clear();
			routingKey = routingKey.trim();
			String[] arr = routingKey.split(",");
			for (String str : arr) {
				this.routingKeys.add(str.trim());
			}
		}

		if (exchangeName.isBlank()) {
			if (this.queueName == null || this.queueName.isBlank()) {
				if (this.routingKeys.size() > 0) {
					this.queueName = this.routingKeys.get(0);
				}
			}
		}
	}
}
