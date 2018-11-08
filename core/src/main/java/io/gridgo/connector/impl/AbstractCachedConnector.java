package io.gridgo.connector.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class AbstractCachedConnector extends AbstractConnector {

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean cacheProducer = false;
	private Producer producer;
	private AtomicBoolean producerInitialized = new AtomicBoolean(false);

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean cacheConsumer = false;
	private Consumer consumer;
	private AtomicBoolean consumerInitialized = new AtomicBoolean(false);

	@Override
	protected final Producer createProducer() {
		if (!cacheProducer) {
			return newProducer();
		} else {
			if (producerInitialized.compareAndSet(false, true)) {
				this.producer = this.newProducer();
			}
			return this.producer;
		}
	}

	protected Producer newProducer() {
		return null;
	}

	@Override
	protected final Consumer createConsumer() {
		if (!cacheConsumer) {
			return newConsumer();
		} else {
			if (consumerInitialized.compareAndSet(false, true)) {
				this.consumer = this.newConsumer();
			}
			return this.consumer;
		}
	}

	protected Consumer newConsumer() {
		return null;
	}
}
