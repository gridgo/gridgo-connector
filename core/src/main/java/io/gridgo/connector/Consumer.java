package io.gridgo.connector;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public interface Consumer extends ComponentLifecycle {

	public Consumer subscribe(
			final @NonNull java.util.function.BiConsumer<Message, Deferred<Message, Exception>> subscriber);

	public default Consumer subscribe(final @NonNull java.util.function.Consumer<Message> subscriber) {
		return subscribe((msg, deferred) -> subscriber.accept(msg));
	}

	public void clearSubscribers();
}
