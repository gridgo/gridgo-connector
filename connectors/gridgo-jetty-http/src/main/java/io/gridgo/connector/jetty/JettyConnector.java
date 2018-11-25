package io.gridgo.connector.jetty;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.jetty.impl.DefaultJettyConsumer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "jetty", syntax = "{type}://{host}[:{port}][/{path}]")
public class JettyConnector extends AbstractConnector {

	@Override
	protected void onInit() {
		String type = getPlaceholder("type");
		String host = getPlaceholder("host");

		String portStr = getPlaceholder("port");
		int port = portStr == null ? 80 : Integer.parseInt(portStr);

		String path = getPlaceholder("path");
		if (path == null || path.isBlank()) {
			path = "/*";
		}

		var jettyConsumer = new DefaultJettyConsumer(getContext(), HostAndPort.newInstance(host, port), path, type);

		this.consumer = Optional.of(jettyConsumer);
		this.producer = Optional.of(jettyConsumer.getResponder());
	}
}
