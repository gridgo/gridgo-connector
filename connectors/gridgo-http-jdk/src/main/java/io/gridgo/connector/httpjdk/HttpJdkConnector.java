package io.gridgo.connector.httpjdk;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "http-jdk,https-jdk", syntax = "httpUri", raw = true)
public class HttpJdkConnector extends AbstractConnector {

	protected void onInit() {
		var endpoint = parseEndpoint();
		var format = getParam(HttpJdkConstants.PARAM_FORMAT);
		var method = getParam(HttpJdkConstants.PARAM_METHOD);
		this.producer = Optional.of(new HttpJdkProducer(getContext(), endpoint, format, method));
	}

	private String parseEndpoint() {
		var scheme = getConnectorConfig().getScheme();
		var endpoint = new StringBuilder();
		if (HttpJdkConstants.SCHEME_HTTP.equals(scheme))
			endpoint.append(HttpJdkConstants.PROTOCOL_HTTP);
		else
			endpoint.append(HttpJdkConstants.PROTOCOL_HTTPS);
		endpoint.append(":");
		endpoint.append(getConnectorConfig().getRemaining());
		return endpoint.toString();
	}
}
