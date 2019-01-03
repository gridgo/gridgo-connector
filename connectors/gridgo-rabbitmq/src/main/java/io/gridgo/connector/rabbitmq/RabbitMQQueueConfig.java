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

    protected static final Collection<String> EXCHANGE_TYPES = Arrays.asList("direct", "fanout", "topic", "headers");

    /**
     * exchange name, default is nameless rabbitMQ default exchange
     */
    private String exchangeName = "";
    /**
     * exchange tpe, accept 4 values: "direct", "fanout", "topic", "headers"
     */
    private String exchangeType = "direct";

    /**
     * affect when the exchangeName is blank, create queue on default nameless
     * exchange
     */
    private String queueName = null;

    /**
     * is queue durable?
     */
    private boolean durable = false;

    /**
     * make the queue visible only on the current connection
     */
    private boolean exclusive = false;

    /**
     * set auto delete when declaring queue
     */
    private boolean autoDelete = false;

    /**
     * auto create response queue for producer, not affect on consumer
     */
    private boolean rpc = false;

    /**
     * only affect on consumer, set autoAck value in channel.basicConsume
     */
    private boolean autoAck = false;

    /**
     * affect on consumer only, the second arg in channel.basicAck method
     */
    private boolean multipleAck = false;

    /**
     * if true, consumer send ack event the process is failed
     */
    private boolean ackOnFail = false;

    /**
     * affect on consumer, bind queue to all the configured routing keys
     */
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

    public RabbitMQQueueConfig makeCopy() {
        RabbitMQQueueConfig result = new RabbitMQQueueConfig();

        result.setExchangeName(this.getExchangeName());
        result.setExchangeType(this.getExchangeType());
        result.setQueueName(this.getQueueName());

        result.setAutoDelete(this.isAutoDelete());
        result.setDurable(this.isDurable());
        result.setExclusive(this.isExclusive());

        result.setAckOnFail(this.isAckOnFail());
        result.setAutoAck(this.isAutoAck());
        result.setMultipleAck(this.isMultipleAck());

        result.setRpc(this.isRpc());

        result.getRoutingKeys().addAll(this.getRoutingKeys());

        return result;
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
        this.ackOnFail = sourceConfig.getBoolean("ackOnFail", this.ackOnFail);

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
