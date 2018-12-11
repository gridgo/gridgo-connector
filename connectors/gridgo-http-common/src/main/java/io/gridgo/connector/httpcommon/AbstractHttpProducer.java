package io.gridgo.connector.httpcommon;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import lombok.Getter;

public abstract class AbstractHttpProducer extends AbstractProducer implements HttpComponent {

    @Getter
    private String format;

    public AbstractHttpProducer(ConnectorContext context, String format) {
        super(context);
        this.format = format;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }
}
