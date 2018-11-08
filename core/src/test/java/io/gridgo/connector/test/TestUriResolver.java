package io.gridgo.connector.test;

import java.util.Properties;

import io.gridgo.connector.Connector;
import io.gridgo.connector.impl.resolvers.UriConnectorResolver;

public class TestUriResolver extends UriConnectorResolver {

	public TestUriResolver(Class<? extends Connector> clazz) {
		super(clazz);
	}
	
	public Properties testResolver(String endpoint, String syntax) {
		return super.extractPlaceholders(endpoint, syntax);
	}
}
