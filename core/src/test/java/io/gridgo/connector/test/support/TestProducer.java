package io.gridgo.connector.test.support;

import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.framework.support.Message;

public class TestProducer extends AbstractProducer {

	@Override
	public void send(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
	}

}
