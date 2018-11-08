package io.gridgo.connector.support.exceptions;

import java.util.function.Function;

import io.gridgo.framework.support.Message;

public interface FailureHandlerAware<T> {

	public T setFailureHandler(Function<Exception, Message> failureHandler);
}
