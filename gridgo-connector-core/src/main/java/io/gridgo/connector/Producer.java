package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;

public interface Producer {

	public void send(Message message);
	
	public Promise<Message, Exception> sendWithAck(Message message);

	public Promise<Message, Exception> call(Message request);
	
	public Producer produceOn(ExecutionStrategy strategy);
	
	public Producer invokeCallbackOn(ExecutionStrategy strategy);
}
