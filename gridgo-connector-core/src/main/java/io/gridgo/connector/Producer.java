package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;

public interface Producer {

	public void send(Message message);
	
	public Promise<Message, Throwable> send(Message message, boolean sendWithAck);

	public Promise<Message, Throwable> call(Message request);
	
	public void produceOn(ExecutionStrategy strategy);
	
	public void invokeCallbackOn(ExecutionStrategy strategy);
}
