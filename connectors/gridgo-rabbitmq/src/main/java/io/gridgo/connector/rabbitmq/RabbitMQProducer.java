package io.gridgo.connector.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.DeliverCallback;

import io.gridgo.connector.Producer;

public interface RabbitMQProducer extends Producer, RabbitMQChannelLifeCycle {

    default String initResponseQueue(DeliverCallback callback) {
        try {
            DeclareOk ok = this.getChannel().queueDeclare();
            final String responseQueueName = ok.getQueue();
            this.getChannel().basicConsume(responseQueueName, false, callback, (String consumerTag) -> {
                getLogger().debug("Response canceled: " + consumerTag);
            });
            return responseQueueName;
        } catch (IOException e) {
            throw new RuntimeException("Cannot declare response queue", e);
        }
    }

    default void publish(byte[] body, BasicProperties props, String routingKey) {
        if (body == null) {
            throw new NullPointerException("Cannot send null body");
        }

        routingKey = routingKey != null ? routingKey : "";
        String exchangeName = this.getQueueConfig().getExchangeName();
        if (exchangeName == null || exchangeName.isBlank()) {
            routingKey = this.getQueueConfig().getQueueName();
        }

        try {
            this.getChannel().basicPublish(exchangeName, routingKey, props, body);
        } catch (IOException e) {
            throw new RuntimeException("Publish message to channel error", e);
        }
    }
}
