package io.gridgo.connector;

import java.util.Optional;

public interface Connector {

	public Optional<Producer> getProducer();

	public Optional<Consumer> getConsumer();
}
