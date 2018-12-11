package io.gridgo.connector.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.gridgo.utils.helper.Loggable;

public interface RabbitMQChannelLifeCycle extends Loggable {

    RabbitMQQueueConfig getQueueConfig();

    Channel getChannel();

    default Channel initChannel(Connection connection) {
        try {
            Channel channel = connection.createChannel();

            String exchangeName = getQueueConfig().getExchangeName();
            if (exchangeName != null && !exchangeName.isBlank()) {
                channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
            }

            String queueName = getQueueConfig().getQueueName();
            if (queueName != null && !queueName.isBlank()) {
                channel.queueDeclare(queueName, getQueueConfig().isDurable(), getQueueConfig().isExclusive(),
                        getQueueConfig().isAutoDelete(), null);
            }

            return channel;
        } catch (Exception e) {
            throw new RuntimeException("Init channel error", e);
        }
    }

    default void closeChannel() {
        try {
            getChannel().close();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Close channel error", e);
        }
    }
}
