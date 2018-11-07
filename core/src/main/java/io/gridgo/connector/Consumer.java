package io.gridgo.connector;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.support.execution.CallbackExecutionAware;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.generators.IdGeneratorAware;
import lombok.NonNull;

public interface Consumer extends ComponentLifecycle, CallbackExecutionAware<Consumer>, IdGeneratorAware {

	public Consumer subscribe(
			final @NonNull java.util.function.BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public default Consumer subscribe(final @NonNull java.util.function.Consumer<Message> subscriber) {
		return subscribe((msg, deferred) -> subscriber.accept(msg));
	}
}
