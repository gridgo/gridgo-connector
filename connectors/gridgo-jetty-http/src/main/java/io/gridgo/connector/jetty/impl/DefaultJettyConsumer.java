package io.gridgo.connector.jetty.impl;

import java.util.Set;

import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.utils.support.HostAndPort;

public class DefaultJettyConsumer extends AbstractJettyConsumer {

    public DefaultJettyConsumer(ConnectorContext context, HostAndPort address, boolean http2Enabled, boolean mmapEnabled, String format, String path,
            Set<JettyServletContextHandlerOption> options) {
        super(context, address, http2Enabled, mmapEnabled, format, path, options);
    }

}
