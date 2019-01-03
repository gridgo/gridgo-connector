package io.gridgo.connector.httpcommon;

import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public abstract class AbstractHttpConsumer extends AbstractConsumer implements HttpComponent {

    @Getter
    private String format;

    public AbstractHttpConsumer(ConnectorContext context, String format) {
        super(context);
        this.format = format;
    }

    protected Message buildFailureMessage(Throwable ex) {
        return getContext().getFailureHandler() //
                           .map(handler -> handler.apply(ex)) //
                           .orElse(null);
    }
}
