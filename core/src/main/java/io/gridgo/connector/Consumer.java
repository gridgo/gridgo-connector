package io.gridgo.connector;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.message.Message;
import io.gridgo.connector.support.execution.CallbackExecutionAware;
import io.gridgo.framework.ComponentLifecycle;
import lombok.NonNull;

public interface Consumer extends ComponentLifecycle, CallbackExecutionAware<Consumer> {

	public Consumer subscribe(final @NonNull java.util.function.BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public Consumer subscribe(final @NonNull java.util.function.Consumer<Message> subscriber);
}
