package io.gridgo.connector.impl;

import java.util.function.Consumer;

import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractHasReceiverProducer extends AbstractProducer implements HasReceiver {

	protected AbstractHasReceiverProducer(ConnectorContext context) {
		super(context);
	}

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Consumer<Message> receiveCallback;
}
