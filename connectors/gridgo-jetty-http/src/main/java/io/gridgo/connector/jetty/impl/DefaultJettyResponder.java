package io.gridgo.connector.jetty.impl;

import io.gridgo.connector.support.config.ConnectorContext;

public class DefaultJettyResponder extends AbstractJettyResponder {

    public DefaultJettyResponder(ConnectorContext context, boolean mmapEnabled, String format, String uniqueIdentifier) {
        super(context, mmapEnabled, format, uniqueIdentifier);
    }

}
