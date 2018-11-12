package io.gridgo.connector;

import java.util.function.Consumer;

import io.gridgo.framework.support.Message;

public interface HasReceiver {

	void setReceiveCallback(Consumer<Message> receiveCallback);
}
