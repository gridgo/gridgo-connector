package io.gridgo.connector.impl.resolvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.exceptions.UnsupportedSchemeException;
import lombok.NonNull;

public class ClasspathConnectorResolver implements ConnectorResolver {

	private final static String DEFAULT_PACKAGE = "io.gridgo.connector";

	private Map<String, Class<? extends Connector>> classMappings = new HashMap<>();

	public ClasspathConnectorResolver() {
		this(DEFAULT_PACKAGE);
	}

	public ClasspathConnectorResolver(final @NonNull String... packages) {
		resolveClasspaths(packages);
	}

	private void resolveClasspaths(String[] packages) {
		for (String pkg : packages) {
			resolvePackage(pkg);
		}
	}

	private void resolvePackage(String pkg) {
		Reflections reflections = new Reflections(pkg);
		Set<Class<? extends Connector>> connectorClasses = reflections.getSubTypesOf(Connector.class);
		
		if (connectorClasses.isEmpty()) {
			// TODO log warning
			return;
		}
		
		for (Class<? extends Connector> clzz : connectorClasses) {
			registerConnectorClass(clzz);
		}
	}

	private void registerConnectorClass(Class<? extends Connector> clzz) {
		ConnectorEndpoint[] endpointAnnotations = clzz.getAnnotationsByType(ConnectorEndpoint.class);
		if (endpointAnnotations.length != 1) {
			// TODO log warning
			return;
		}
		ConnectorEndpoint endpoint = endpointAnnotations[0];
		String scheme = endpoint.scheme();
		if (classMappings.containsKey(scheme)) {
			// TODO log warning
		} else {
			classMappings.put(scheme, clzz);
		}
	}

	@Override
	public Connector resolve(final @NonNull String endpoint) {
		String scheme = endpoint, remaining = "";
		int schemeIdx = endpoint.indexOf(':');
		if (schemeIdx != -1) {
			scheme = endpoint.substring(0, schemeIdx);
			remaining = endpoint.substring(schemeIdx + 1);
		}

		Class<? extends Connector> clzz = classMappings.get(scheme);
		if (clzz == null)
			throw new UnsupportedSchemeException(scheme);
		return new UriConnectorResolver(clzz).resolve(remaining);
	}
}
