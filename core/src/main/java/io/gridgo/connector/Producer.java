package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public interface Producer extends ComponentLifecycle {

	public void send(final @NonNull Message message);

	public Promise<Message, Exception> sendWithAck(final @NonNull Message message);

	public Promise<Message, Exception> call(final @NonNull Message request);
}
