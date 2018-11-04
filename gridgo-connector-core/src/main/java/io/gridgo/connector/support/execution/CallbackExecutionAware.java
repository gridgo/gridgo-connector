package io.gridgo.connector.support.execution;

import io.gridgo.connector.Consumer;
import io.gridgo.framework.execution.ExecutionStrategy;

public interface CallbackExecutionAware {

	public Consumer invokeCallbackOn(final ExecutionStrategy strategy);
}
