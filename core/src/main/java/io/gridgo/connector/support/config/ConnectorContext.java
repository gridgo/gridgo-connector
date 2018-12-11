package io.gridgo.connector.support.config;

import java.util.Optional;
import java.util.function.Consumer;

import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.RegistryAware;
import io.gridgo.framework.support.generators.IdGenerator;

public interface ConnectorContext extends RegistryAware {

    public Registry getRegistry();

    public IdGenerator getIdGenerator();

    public Consumer<Throwable> getExceptionHandler();

    public ExecutionStrategy getCallbackInvokerStrategy();

    public Optional<ExecutionStrategy> getConsumerExecutionStrategy();

    public ExecutionStrategy getProducerExecutionStrategy();
}
