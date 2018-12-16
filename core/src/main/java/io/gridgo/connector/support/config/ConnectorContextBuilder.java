package io.gridgo.connector.support.config;

import java.util.function.Consumer;
import java.util.function.Function;

import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Builder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.generators.IdGenerator;

public interface ConnectorContextBuilder extends Builder<ConnectorContext> {

    public ConnectorContextBuilder setRegistry(Registry registry);

    public ConnectorContextBuilder setIdGenerator(IdGenerator idGenerator);

    public ConnectorContextBuilder setExceptionHandler(Consumer<Throwable> exceptionHandler);

    public ConnectorContextBuilder setFailureHandler(Function<Throwable, Message> failureHandler);

    public ConnectorContextBuilder setCallbackInvokerStrategy(ExecutionStrategy strategy);

    public ConnectorContextBuilder setConsumerExecutionStrategy(ExecutionStrategy strategy);

    public ConnectorContextBuilder setProducerExecutionStrategy(ExecutionStrategy strategy);
}
