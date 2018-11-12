package io.gridgo.connector.mongodb;

import com.mongodb.async.client.MongoClient;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "mongodb", syntax = "{connectionBean}")
public class MongoDBConnector extends AbstractConnector {

	protected void onInit() {
		var connectionBean = getConnectorConfig().getPlaceholders().get("connectionBean").toString();
		var connection = getContext().getRegistry().lookup(connectionBean, MongoClient.class);
	}
}
