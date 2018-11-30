package io.gridgo.connector.httpcommon;

import java.util.function.Function;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public abstract class AbstractHttpConsumer extends AbstractConsumer
		implements HttpComponent, FailureHandlerAware<Consumer> {

	@Getter
	private String format;

	@Getter
	private Function<Throwable, Message> failureHandler;

	public AbstractHttpConsumer(ConnectorContext context, String format) {
		super(context);
		this.format = format;
	}

	protected Message buildFailureMessage(Throwable ex) {
		return failureHandler != null ? failureHandler.apply(ex) : null;
	}

	@Override
	public Consumer setFailureHandler(Function<Throwable, Message> failureHandler) {
		this.failureHandler = failureHandler;
		return this;
	}
}
