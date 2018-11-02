package io.gridgo.connector;

public interface Connector {

	public Producer getProducer();

	public Consumer getConsumer();
}
