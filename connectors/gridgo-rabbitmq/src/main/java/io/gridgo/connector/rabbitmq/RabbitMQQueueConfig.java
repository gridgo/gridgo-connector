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

	private boolean autoDelete = false;

	private boolean rpc = false;

	private boolean autoAck = false;

	private boolean multipleAck = false;

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

	public void readFromBObject(final BObject sourceConfig) {
		this.exchangeName = sourceConfig.getString("exchangeName", this.exchangeName);
		if (this.exchangeName == null) {
			this.exchangeName = "";
		}
		this.exchangeName = this.exchangeName.trim();

		this.exchangeType = sourceConfig.getString("exchangeType", this.exchangeType);
		if (exchangeType == null || !EXCHANGE_TYPES.contains(exchangeType.trim())) {
			throw new InvalidParamException("Exchange type invalid, expect one of " + EXCHANGE_TYPES);
		}

		this.queueName = sourceConfig.getString("queueName", this.queueName);

		this.durable = sourceConfig.getBoolean("durable", this.durable);
		this.exclusive = sourceConfig.getBoolean("exclusive", this.exclusive);
		this.autoDelete = sourceConfig.getBoolean("autoDelete", this.autoDelete);

		this.autoAck = sourceConfig.getBoolean("autoAck", this.autoAck);
		this.multipleAck = sourceConfig.getBoolean("multipleAck", this.multipleAck);

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
	}
}
