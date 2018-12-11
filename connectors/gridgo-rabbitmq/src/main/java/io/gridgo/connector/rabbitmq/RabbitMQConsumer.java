package io.gridgo.connector.rabbitmq;

import java.io.IOException;
import java.util.List;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.DeliverCallback;

import io.gridgo.connector.Consumer;

public interface RabbitMQConsumer extends Consumer, RabbitMQChannelLifeCycle {

    default void subscibe(DeliverCallback onMessageCallback, CancelCallback onCancelCallback) {
        try {
            String queueName = getQueueConfig().getQueueName();

            if (queueName == null || queueName.isBlank()) {
                queueName = getChannel().queueDeclare().getQueue();
                this.getQueueConfig().setQueueName(queueName);
            }

            String exchangeName = getQueueConfig().getExchangeName();

            if (exchangeName != null && !exchangeName.isBlank()) {
                List<String> routingKeys = getQueueConfig().getRoutingKeys();
                if (routingKeys.size() > 0) {
                    for (String routingKey : routingKeys) {
                        getChannel().queueBind(queueName, exchangeName, routingKey);
                    }
                } else {
                    getChannel().queueBind(queueName, exchangeName, "");
                }
            }

            boolean autoAck = getQueueConfig().isAutoAck();
            this.getChannel().basicConsume(queueName, autoAck, onMessageCallback, onCancelCallback);

        } catch (IOException e) {
            throw new RuntimeException("Cannot init basic consume", e);
        }
    }
}
