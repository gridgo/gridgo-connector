package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.message.Message;
import io.gridgo.connector.support.execution.CallbackExecutionAware;
import io.gridgo.framework.ComponentLifecycle;
import lombok.NonNull;

public interface Producer extends ComponentLifecycle, CallbackExecutionAware<Producer> {

	public void send(final @NonNull Message message);
	
	public Promise<Message, Exception> sendWithAck(final @NonNull Message message);

	public Promise<Message, Exception> call(final @NonNull Message request);
}
