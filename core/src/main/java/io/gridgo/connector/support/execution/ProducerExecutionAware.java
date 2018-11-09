package io.gridgo.connector.support.execution;

import io.gridgo.framework.execution.ExecutionStrategy;

public interface ProducerExecutionAware<T> {

	public T produceOn(final ExecutionStrategy strategy);
}
