package io.gridgo.connector;

import io.gridgo.connector.message.Message;

public interface Consumer {

	public void subscribe(java.util.function.Consumer<Message> subscriber);
}
