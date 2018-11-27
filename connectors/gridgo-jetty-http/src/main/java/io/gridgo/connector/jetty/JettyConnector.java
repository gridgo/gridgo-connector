package io.gridgo.connector.jetty;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.jetty.impl.DefaultJettyConsumer;
import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "jetty", syntax = "http://{host}[:{port}][/{path}]")
public class JettyConnector extends AbstractConnector {

	@Override
	protected void onInit() {
		String host = getPlaceholder("host");

		String portStr = getPlaceholder("port");
		int port = portStr == null ? 80 : Integer.parseInt(portStr);

		String path = getPlaceholder("path");
		if (path == null || path.isBlank()) {
			path = "/*";
		}

		Set<JettyServletContextHandlerOption> options = readOptions();
		boolean http2Enabled = Boolean.valueOf(getParam("http2Enabled", "true"));

		var jettyConsumer = new DefaultJettyConsumer(getContext(), HostAndPort.newInstance(host, port), http2Enabled,
				path, options);

		this.consumer = Optional.of(jettyConsumer);
		this.producer = Optional.of(jettyConsumer.getResponder());
	}

	private Set<JettyServletContextHandlerOption> readOptions() {
		Set<JettyServletContextHandlerOption> options = new HashSet<>();

		if (Boolean.parseBoolean(getParam("session", "false"))) {
			options.add(JettyServletContextHandlerOption.SESSIONS);
		} else {
			options.add(JettyServletContextHandlerOption.NO_SESSIONS);
		}

		if (Boolean.parseBoolean(getParam("security", "false"))) {
			options.add(JettyServletContextHandlerOption.SECURITY);
		} else {
			options.add(JettyServletContextHandlerOption.NO_SECURITY);
		}

		if (Boolean.parseBoolean(getParam("gzip", "false"))) {
			options.add(JettyServletContextHandlerOption.GZIP);
		}

		return options;
	}
}
