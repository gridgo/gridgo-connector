package io.gridgo.connector;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;
import lombok.NonNull;

public interface Consumer {

	public Consumer subscribe(final @NonNull java.util.function.BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public Consumer subscribe(final @NonNull java.util.function.Consumer<Message> subscriber);

	public Consumer invokeCallbackOn(final ExecutionStrategy strategy);
}
