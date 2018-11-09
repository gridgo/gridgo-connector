package io.gridgo.connector.support.execution;

import io.gridgo.framework.execution.ExecutionStrategy;

public interface ConsumerExecutionAware<T> {

	public T consumeOn(final ExecutionStrategy strategy);
}
