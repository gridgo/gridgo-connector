package io.gridgo.connector.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.execution.ConsumerExecutionAware;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.disruptor.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public class KafkaConsumer extends AbstractConsumer implements ConsumerExecutionAware<Consumer> {

	private static final int DEFAULT_THREADS = 8;

	private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new ExecutorExecutionStrategy(DEFAULT_THREADS);;

	private ExecutionStrategy consumerExecutionStrategy = DEFAULT_EXECUTION_STRATEGY;

	private final KafkaConfiguration configuration;

	private List<KafkaFetchRecords> tasks;

	public KafkaConsumer(final @NonNull KafkaConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void onStart() {
		consumerExecutionStrategy.start();

		tasks = new ArrayList<>();

		Properties props = getProps();

		for (int i = 0; i < configuration.getConsumersCount(); i++) {
			KafkaFetchRecords task = new KafkaFetchRecords(configuration.getTopic(), i + "", props);
			consumerExecutionStrategy.execute(task);
			tasks.add(task);
		}
	}

	private Properties getProps() {
		return configuration.createConsumerProperties();
	}

	@Override
	protected void onStop() {
		for (KafkaFetchRecords task : tasks) {
			task.shutdown();
		}
		consumerExecutionStrategy.stop();
	}

	@Override
	public Consumer consumeOn(final @NonNull ExecutionStrategy strategy) {
		this.consumerExecutionStrategy = strategy;
		return this;
	}

	class KafkaFetchRecords implements Runnable {

		private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

		private final String topicName;

		private final Properties kafkaProps;

		private String id;

		public KafkaFetchRecords(String topicName, String id, Properties kafkaProps) {
			this.topicName = topicName;
			this.id = id;
			this.kafkaProps = kafkaProps;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("KAFKA-CONSUMER-" + topicName + "-" + id);

			Duration pollDuration = Duration.ofMillis(100);

			try {
				consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(kafkaProps);
				consumer.subscribe(Arrays.asList(topicName.split(",")));

				if (configuration.getSeekTo() != null) {
					if (configuration.getSeekTo().equals("beginning")) {
						// This poll to ensures we have an assigned partition otherwise seek won't work
						consumer.poll(pollDuration);
						consumer.seekToBeginning(consumer.assignment());
					} else if (configuration.getSeekTo().equals("end")) {
						// This poll to ensures we have an assigned partition otherwise seek won't work
						consumer.poll(pollDuration);
						consumer.seekToEnd(consumer.assignment());
					} else {
						throw new IllegalArgumentException("Invalid seekTo option: " + configuration.getSeekTo());
					}
				}

				while (Thread.currentThread().isInterrupted()) {
					// flag to break out processing on the first exception
					boolean breakOnErrorHit = false;
					ConsumerRecords<Object, Object> allRecords = consumer.poll(pollDuration);

					for (TopicPartition partition : allRecords.partitions()) {

						Iterator<ConsumerRecord<Object, Object>> recordIterator = allRecords.records(partition)
								.iterator();
						if (!breakOnErrorHit && recordIterator.hasNext()) {
							ConsumerRecord<Object, Object> record;

							while (!breakOnErrorHit && recordIterator.hasNext()) {
								record = recordIterator.next();

								Message msg = buildMessage(record);

								// if not auto commit then we have additional information on the exchange
								if (!configuration.isAutoCommitEnable()) {
									msg.getMisc().put(KafkaConstants.LAST_RECORD_BEFORE_COMMIT,
											!recordIterator.hasNext());
								}

								publish(msg, null);
							}
						}
					}
				}

				if (configuration.isAutoCommitEnable()) {
					if ("async".equals(configuration.getAutoCommitOnStop())) {
						consumer.commitAsync();
					} else if ("sync".equals(configuration.getAutoCommitOnStop())) {
						consumer.commitSync();
					}
				}
			} catch (Exception e) {
				// TODO log error
				e.printStackTrace();
			} finally {
				consumer.unsubscribe();
			}
		}

		private Message buildMessage(ConsumerRecord<Object, Object> record) {
			BObject headers = BObject.newDefault();

			headers.putAny(KafkaConstants.PARTITION, record.partition());
			headers.putAny(KafkaConstants.TOPIC, record.topic());
			headers.putAny(KafkaConstants.OFFSET, record.offset());
			headers.putAny(KafkaConstants.TIMESTAMP, record.timestamp());
			if (record.key() != null) {
				headers.putAny(KafkaConstants.KEY, record.key());
			}

			for (Header header : record.headers()) {
				headers.putAny(header.key(), header.value());
			}

			BElement body = BObject.newDefault(record.value());
			return createMessage(headers, body);
		}

		private void shutdown() {
			// As advised in the KAFKA-1894 ticket, calling this wakeup method breaks the
			// infinite loop
			if (consumer != null)
				consumer.wakeup();
		}
	}
}
