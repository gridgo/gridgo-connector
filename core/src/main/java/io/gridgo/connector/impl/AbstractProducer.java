package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Producer;
import io.gridgo.connector.ProducerAck;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.impl.AbstractComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.DefaultPayload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

    @Getter
    private final ConnectorContext context;

    private ProducerAck ack;

    protected AbstractProducer(final @NonNull ConnectorContext context) {
        this.context = context;
        this.ack = new DefaultProducerAck(context);
    }

    protected void ack(Deferred<Message, Exception> deferred) {
        ack.ack(deferred);
    }

    protected void ack(Deferred<Message, Exception> deferred, Exception exception) {
        ack.ack(deferred, exception);
    }

    protected void ack(Deferred<Message, Exception> deferred, Message response) {
        ack.ack(deferred, response);
    }

    protected void ack(Deferred<Message, Exception> deferred, Message response, Exception exception) {
        ack(deferred, response, exception);
    }

    protected Message createMessage(BObject headers, BElement body) {
        return Message.of(new DefaultPayload(context.getIdGenerator().generateId(), headers, body));
    }
}
