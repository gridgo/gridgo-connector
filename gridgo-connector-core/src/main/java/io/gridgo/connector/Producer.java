package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;
import lombok.NonNull;

public interface Producer {

	public void send(final @NonNull Message message);
	
	public Promise<Message, Exception> sendWithAck(final @NonNull Message message);

	public Promise<Message, Exception> call(final @NonNull Message request);
	
	public Producer produceOn(final ExecutionStrategy strategy);
	
	public Producer invokeCallbackOn(final ExecutionStrategy strategy);
}
