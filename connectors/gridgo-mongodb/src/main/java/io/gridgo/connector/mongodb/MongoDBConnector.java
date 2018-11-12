package io.gridgo.connector.mongodb;

import java.util.Optional;

import com.mongodb.async.client.MongoClient;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "mongodb", syntax = "{connectionBean}/{database}[/{collection}]")
public class MongoDBConnector extends AbstractConnector {

	protected void onInit() {
		var connectionBean = getConnectorConfig().getPlaceholders().get("connectionBean").toString();
		var database = getConnectorConfig().getPlaceholders().get("database").toString();
		var collection = getConnectorConfig().getPlaceholders().get("collection");
		var collectionName = collection != null ? collection.toString() : null;

		var connection = getContext().getRegistry().lookup(connectionBean, MongoClient.class);
		if (connection == null)
			throw new RuntimeException("Bean " + connectionBean + " cannot be found");
		var mongoCollection = connection.getDatabase(database).getCollection(collectionName);
		this.producer = Optional.of(new MongoDBProducer(getContext(), mongoCollection));
	}
}
