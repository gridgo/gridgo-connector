package io.gridgo.connector.impl;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.framework.AbstractComponentLifecycle;
import lombok.Getter;

public abstract class AbstractConnector extends AbstractComponentLifecycle implements Connector {

	protected static final String LOCALHOST = "localhost";

	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	@Getter
	private ConnectorConfig connectorConfig;
	
	protected Optional<Consumer> consumer = Optional.empty();

	protected Optional<Producer> producer = Optional.empty();

	@Override
	public final Connector initialize(ConnectorConfig config) {
		if (initialized.compareAndSet(false, true)) {
			this.connectorConfig = config;
			this.onInit();
			return this;
		}
		throw new IllegalStateException("Cannot re-init connector of type " + this.getClass().getName());
	}

	@Override
	public final Optional<Producer> getProducer() {
		return producer;
	}

	@Override
	public final Optional<Consumer> getConsumer() {
		return consumer;
	}
	
	protected String getParam(ConnectorConfig config, String name) {
		Object value = config.getParameters().get(name);
		return value != null ? value.toString() : null;
	}

	protected String getParam(ConnectorConfig config, String name, String defaultValue) {
		Object value = config.getParameters().getOrDefault(name, defaultValue);
		return value != null ? value.toString() : null;
	}

	protected void onInit() {
		// do nothing
	}

	@Override
	protected void onStart() {
		// do nothing
	}

	@Override
	protected void onStop() {
		// do nothing
	}

}
