package io.gridgo.connector;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;

public interface Consumer {

	public Consumer subscribe(java.util.function.BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public Consumer subscribe(java.util.function.Consumer<Message> subscriber);

	public Consumer invokeCallbackOn(ExecutionStrategy strategy);
}
