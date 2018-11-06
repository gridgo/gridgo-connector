package io.gridgo.connector.support.execution;

import io.gridgo.framework.execution.ExecutionStrategy;

public interface CallbackExecutionAware<T> {

	public T invokeCallbackOn(final ExecutionStrategy strategy);
}
