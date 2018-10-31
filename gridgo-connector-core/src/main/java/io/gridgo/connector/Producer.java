package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.message.Message;

public interface Producer {

	void send(Message message);

	Promise<Message, Throwable> call(Message request);
}
