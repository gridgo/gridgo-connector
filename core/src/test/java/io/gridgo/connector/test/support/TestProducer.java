package io.gridgo.connector.test.support;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.junit.Assert;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.generators.impl.UUIDGenerator;

public class TestProducer extends AbstractProducer {

	@Override
	public void send(Message message) {

	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		return new SimpleDonePromise<>(null);
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		int body = request.getPayload().getBody().asValue().getInteger();
		return new SimpleDonePromise<>(Message.newDefault(Payload.newDefault(BValue.newDefault(body + 1))));
	}

	@Override
	protected void onStart() {
		var strategy = new DefaultExecutionStrategy();
		var generator = new UUIDGenerator();
		produceOn(strategy).invokeCallbackOn(strategy).setIdGenerator(generator);
		Assert.assertTrue(getProducerExecutionStrategy() == strategy);
		Assert.assertTrue(getCallbackInvokeExecutor() == strategy);
		Assert.assertTrue(getIdGenerator() == generator);
	}

	@Override
	protected void onStop() {

	}

}
