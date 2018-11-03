package io.gridgo.connector.impl.resolvers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.ConnectorResolutionException;

public class UriConnectorResolver implements ConnectorResolver {

	private final Class<? extends Connector> clazz;

	public UriConnectorResolver(Class<? extends Connector> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Connector resolve(String endpoint) {
		try {
			ConnectorConfig config = resolveProperties(endpoint);
			return clazz.getConstructor(ConnectorConfig.class).newInstance(config);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ConnectorResolutionException("Exception caught while resolving endpoint " + endpoint, e);
		}
	}

	private ConnectorConfig resolveProperties(String endpoint) {
		String schemePart = endpoint;
		String queryPart = null;

		int queryPartIdx = endpoint.indexOf('?');
		if (queryPartIdx != -1) {
			queryPart = endpoint.substring(queryPartIdx + 1);
			schemePart = endpoint.substring(0, queryPartIdx);
		}

		Map<String, Object> params = extractParameters(queryPart);
		return new DefaultConnectorConfig(schemePart, params);
	}

	private Map<String, Object> extractParameters(String queryPath) {
		Map<String, Object> params = new HashMap<>();

		String[] queries = queryPath.split("&");
		for (String query : queries) {
			String[] keyValuePair = query.split("=");
			if (keyValuePair.length == 2)
				params.put(keyValuePair[0], keyValuePair[1]);
		}

		return params;
	}
}
