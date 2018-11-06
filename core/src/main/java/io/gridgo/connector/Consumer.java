package io.gridgo.connector;

import java.util.function.BiConsumer;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.support.execution.CallbackExecutionAware;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;

public interface Consumer extends ComponentLifecycle, CallbackExecutionAware<Consumer> {

	public Consumer subscribe(final BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public Consumer subscribe(final java.util.function.Consumer<Message> subscriber);
}
