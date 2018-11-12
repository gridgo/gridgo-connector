package io.gridgo.connector.impl;

import java.util.function.Consumer;

import io.gridgo.connector.HasReceiver;
import io.gridgo.framework.support.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractHasReceiverProducer extends AbstractProducer implements HasReceiver {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Consumer<Message> receiveCallback;
}
