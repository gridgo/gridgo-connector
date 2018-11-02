package io.gridgo.connector;

import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.message.Message;

public interface Consumer {

	public void subscribe(BiConsumer<Message, Deferred<Message, Throwable>> subscriber);
}
