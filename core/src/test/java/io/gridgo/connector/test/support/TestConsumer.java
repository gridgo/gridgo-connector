package io.gridgo.connector.test.support;

import java.util.function.Consumer;

import org.junit.Assert;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.generators.impl.UUIDGenerator;

public class TestConsumer extends AbstractConsumer {

	@Override
	protected void onStart() {
		var strategy = new DefaultExecutionStrategy();
		var handler = (Consumer<Throwable>) ex -> ex.printStackTrace();
		var generator = new UUIDGenerator();
		consumeOn(strategy).invokeCallbackOn(strategy).setExceptionHandler(handler).setIdGenerator(generator);;
		Assert.assertTrue(getConsumerExecutionStrategy() == strategy);
		Assert.assertTrue(getCallbackInvokeExecutor() == strategy);
		Assert.assertTrue(getExceptionHandler() == handler);
		Assert.assertTrue(getIdGenerator() == generator);
	}

	@Override
	protected void onStop() {

	}

	public void testPublish() {
		publish(createMessage(BObject.newDefault().setAny("test-header", 1), null), null);
	}

}
