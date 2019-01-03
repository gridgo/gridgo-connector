package io.gridgo.connector.mongodb;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "mongodb", syntax = "{connectionBean}/{database}[/{collection}]")
public class MongoDBConnector extends AbstractConnector {

    protected void onInit() {
        var connectionBean = getPlaceholder("connectionBean").toString();
        var database = getPlaceholder("database").toString();
        var collection = getPlaceholder("collection");
        var collectionName = collection != null ? collection.toString() : null;

        this.producer = Optional.of(new MongoDBProducer(getContext(), connectionBean, database, collectionName));
    }
}
