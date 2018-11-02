package io.gridgo.connector;

import io.gridgo.connector.message.Message;
import io.gridgo.framework.execution.ExecutionStrategy;

public interface Consumer {

	public void subscribe(java.util.function.Consumer<Message> subscriber);
	
	public void invokeCallbackOn(ExecutionStrategy strategy);
}
