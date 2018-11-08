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

	@Override
	public final Connector initialize(ConnectorConfig config) {
		if (initialized.compareAndSet(false, true)) {
			this.connectorConfig = config;
			this.onInit();
			return this;
		}
		throw new IllegalStateException("Cannot re-init connector of type " + this.getClass().getName());
	}

	protected void onInit() {

	}

	@Override
	public final Optional<Producer> getProducer() {
		return Optional.ofNullable(this.createProducer());
	}

	protected Producer createProducer() {
		return null;
	}

	@Override
	public final Optional<Consumer> getConsumer() {
		return Optional.ofNullable(this.createConsumer());
	}

	protected Consumer createConsumer() {
		return null;
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
