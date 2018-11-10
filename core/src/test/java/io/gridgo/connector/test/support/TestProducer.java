package io.gridgo.connector.test.support;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleDonePromise;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

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

	}

	@Override
	protected void onStop() {

	}

}
