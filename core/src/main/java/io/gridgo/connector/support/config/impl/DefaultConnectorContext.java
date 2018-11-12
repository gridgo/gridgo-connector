package io.gridgo.connector.support.config.impl;

import java.util.Optional;
import java.util.function.Consumer;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.generators.IdGenerator;
import io.gridgo.framework.support.impl.SimpleRegistry;
import lombok.Getter;

@Getter
public class DefaultConnectorContext implements ConnectorContext {

	private static final ExecutionStrategy DEFAULT_CALLBACK_EXECUTOR = new DefaultExecutionStrategy();

	private static final java.util.function.Consumer<Throwable> DEFAULT_EXCEPTION_HANDLER = ex -> {
	};

	private Optional<IdGenerator> idGenerator = Optional.empty();

	private Registry registry = new SimpleRegistry();

	private Consumer<Throwable> exceptionHandler = DEFAULT_EXCEPTION_HANDLER;

	private ExecutionStrategy callbackInvokerStrategy = DEFAULT_CALLBACK_EXECUTOR;

	private Optional<ExecutionStrategy> consumerExecutionStrategy = Optional.empty();

	private ExecutionStrategy producerExecutionStrategy = DEFAULT_CALLBACK_EXECUTOR;

	public DefaultConnectorContext() {

	}

	public DefaultConnectorContext(IdGenerator idGenerator, Registry registry, Consumer<Throwable> exceptionHandler,
			ExecutionStrategy callbackInvokerStrategy, ExecutionStrategy consumerExecutionStrategy,
			ExecutionStrategy producerExecutionStrategy) {
		this.idGenerator = Optional.ofNullable(idGenerator);
		if (registry != null)
			this.registry = registry;
		if (exceptionHandler != null)
			this.exceptionHandler = exceptionHandler;
		if (callbackInvokerStrategy != null)
			this.callbackInvokerStrategy = callbackInvokerStrategy;
		this.consumerExecutionStrategy = Optional.ofNullable(consumerExecutionStrategy);
		if (producerExecutionStrategy != null)
			this.producerExecutionStrategy = producerExecutionStrategy;
	}
}
