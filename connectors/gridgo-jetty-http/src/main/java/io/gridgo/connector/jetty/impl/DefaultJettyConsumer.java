package io.gridgo.connector.jetty.impl;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.utils.support.HostAndPort;

public class DefaultJettyConsumer extends AbstractJettyConsumer {

	public DefaultJettyConsumer(ConnectorContext context, HostAndPort address, String path, String httpType) {
		super(context, address, path, httpType);
	}

}
