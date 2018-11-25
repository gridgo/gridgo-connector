package io.gridgo.connector.jetty.impl;

import java.util.Set;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.jetty.JettyServletContextHandlerOption;
import io.gridgo.utils.support.HostAndPort;

public class DefaultJettyConsumer extends AbstractJettyConsumer {

	public DefaultJettyConsumer(ConnectorContext context, HostAndPort address, String path, String httpType,
			Set<JettyServletContextHandlerOption> options) {
		super(context, address, path, httpType, options);
	}

}
