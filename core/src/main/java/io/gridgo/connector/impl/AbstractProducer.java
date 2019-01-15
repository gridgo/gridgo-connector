package io.gridgo.connector.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.impl.AbstractComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.DefaultPayload;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractProducer extends AbstractComponentLifecycle implements Producer {

    @Getter
    private final ConnectorContext context;

    protected AbstractProducer(final @NonNull ConnectorContext context) {
        this.context = context;
    }

    protected void ack(Deferred<Message, Exception> deferred) {
        if (deferred != null) {
            context.getCallbackInvokerStrategy().execute(() -> tryResolve(deferred, null));
        }
    }

    protected void ack(Deferred<Message, Exception> deferred, Exception exception) {
        if (exception != null)
            getLogger().error("Exception caught while acknowledging response", exception);
        if (deferred != null) {
            context.getCallbackInvokerStrategy().execute(() -> {
                if (exception == null) {
                    tryResolve(deferred, null);
                } else {
                    deferred.reject(exception);
                }
            });
        }
    }

    protected void ack(Deferred<Message, Exception> deferred, Message response) {
        if (deferred != null) {
            context.getCallbackInvokerStrategy().execute(() -> tryResolve(deferred, response));
        }
    }

    protected void ack(Deferred<Message, Exception> deferred, Message response, Exception exception) {
        if (exception != null)
            ack(deferred, exception);
        else
            ack(deferred, response);
    }

    protected Message createMessage(BObject headers, BElement body) {
        return Message.of(new DefaultPayload(context.getIdGenerator().generateId(), headers, body));
    }

    private Deferred<Message, Exception> tryResolve(Deferred<Message, Exception> deferred, Message response) {
        try {
            return deferred.resolve(response);
        } catch (Exception ex) {
            getLogger().error("Exception caught while trying to resolve deferred", ex);
            return deferred.reject(ex);
        }
    }
}
