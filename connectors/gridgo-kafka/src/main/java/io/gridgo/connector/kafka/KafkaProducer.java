package io.gridgo.connector.kafka;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.JoinedPromise;
import org.joo.promise4j.impl.JoinedResults;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;

public class KafkaProducer extends AbstractProducer {

	private KafkaConfiguration configuration;

	private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer;

	private String[] topics;

	public KafkaProducer(ConnectorContext context, KafkaConfiguration configuration) {
		super(context);
		this.configuration = configuration;
		this.topics = configuration.getTopic().split(",");
	}

	@Override
	public void send(Message message) {
		for (var topic : topics) {
			var record = buildProducerRecord(topic, message);
			this.producer.send(record);
		}
	}

	@Override
	public Promise<Message, Exception> sendWithAck(Message message) {
		var promises = new ArrayList<Promise<Message, Exception>>();
		for (var topic : topics) {
			var deferred = new CompletableDeferredObject<Message, Exception>();
			var record = buildProducerRecord(topic, message);
			this.producer.send(record, (metadata, ex) -> ack(deferred, metadata, ex));
			promises.add(deferred);
		}

		return promises.size() == 1 ? promises.get(0)
				: JoinedPromise.from(promises).filterDone(this::convertJoinedResult);
	}

	public Message convertJoinedResult(JoinedResults<Message> results) {
		return new MultipartMessage(results);
	}

	private void ack(Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
		var msg = buildAckMessage(metadata);
		ack(deferred, msg, exception);
	}

	private ProducerRecord<Object, Object> buildProducerRecord(String topic, Message message) {
		var headers = message.getPayload().getHeaders();

		var partitionValue = headers.getValue(KafkaConstants.PARTITION);
		Integer partition = partitionValue != null ? partitionValue.getInteger() : null;
		var timestampValue = headers.getValue(KafkaConstants.TIMESTAMP);
		Long timestamp = timestampValue != null ? timestampValue.getLong() : null;
		var keyValue = headers.getValue(KafkaConstants.KEY);
		Object key = keyValue != null ? keyValue.getData() : null;
		var value = convert(message.getPayload().getBody());
		
		return new ProducerRecord<Object, Object>(topic, partition, timestamp, key, value);
	}

	private Object convert(BElement body) {
		if (body == null)
			return null;
		if (body.isValue())
			return body.asValue().getData();
		return body.toBytes();
	}

	private Message buildAckMessage(RecordMetadata metadata) {
		if (metadata == null)
			return null;
		var headers = BObject.newDefault().setAny(KafkaConstants.IS_ACK_MSG, "true")
				.setAny(KafkaConstants.TIMESTAMP, metadata.timestamp()).setAny(KafkaConstants.OFFSET, metadata.offset())
				.setAny(KafkaConstants.PARTITION, metadata.partition()).setAny(KafkaConstants.TOPIC, metadata.topic());
		return createMessage(headers, BValue.newDefault());
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void onStart() {
		if (configuration.isTopicIsPattern())
			getLogger().warn("topicIsPattern won't work with KafkaProducer, will ignore");
		var props = getProps();
		var threadClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			// Kafka uses reflection for loading authentication settings, use its
			// classloader
			Thread.currentThread()
					.setContextClassLoader(org.apache.kafka.clients.producer.KafkaProducer.class.getClassLoader());
			this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
		} finally {
			Thread.currentThread().setContextClassLoader(threadClassLoader);
		}
	}

	@Override
	protected void onStop() {
		if (this.producer != null)
			this.producer.close();
	}

	private Properties getProps() {
		return configuration.createProducerProperties();
	}

	@Override
	protected String generateName() {
		return "producer.kafka." + configuration.getTopic();
	}

	@Override
	public boolean isCallSupported() {
		return false;
	}
}
