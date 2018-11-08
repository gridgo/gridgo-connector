package io.gridgo.connector.kafka;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.execution.ProducerExecutionAware;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Message;

public class KafkaProducer extends AbstractProducer implements ProducerExecutionAware<Consumer> {

	public KafkaProducer(KafkaConfiguration kafkaConfig) {
		// TODO Auto-generated constructor stub
	}

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
	public Consumer produceOn(ExecutionStrategy strategy) {
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
