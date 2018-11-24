package io.gridgo.connector.jetty;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "jetty", syntax = "{type}://{host}[:{port}][/{path}]")
public class JettyConnector extends AbstractConnector {

	@Override
	protected void onInit() {

	}
}
