package io.gridgo.connector;

public interface Connector {

	Producer getProducer();

	Consumer getConsumer();
}
