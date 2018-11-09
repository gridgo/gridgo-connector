package io.gridgo.connector.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.impl.BFactory;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.execution.ConsumerExecutionAware;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

public class KafkaConsumer extends AbstractConsumer implements ConsumerExecutionAware<Consumer> {

	private static final int DEFAULT_THREADS = 8;

	private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new ExecutorExecutionStrategy(DEFAULT_THREADS);

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

		var props = getProps();

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
		if (consumerExecutionStrategy != DEFAULT_EXECUTION_STRATEGY)
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

		private volatile boolean stopped = false;

		public KafkaFetchRecords(String topicName, String id, Properties kafkaProps) {
			this.topicName = topicName;
			this.id = id;
			this.kafkaProps = kafkaProps;
		}

		@Override
		public void run() {
			stopped = false;
			Thread.currentThread().setName("KAFKA-CONSUMER-" + topicName + "-" + id);

			var pollDuration = Duration.ofMillis(100);
			var batchProcessing = configuration.isBatchEnabled();

			try {
				consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(kafkaProps);
				consumer.subscribe(Arrays.asList(topicName.split(",")));

				seekOffset(pollDuration);

				while (!stopped && !Thread.currentThread().isInterrupted()) {

					var allRecords = consumer.poll(pollDuration);

					for (var partition : allRecords.partitions()) {

						List<ConsumerRecord<Object, Object>> records = allRecords.records(partition);

						if (records.isEmpty())
							continue;

						Promise<Long, Exception> promise;

						if (batchProcessing) {
							promise = processBatchRecords(records);
						} else {
							promise = processSingleRecord(partition, records);
						}

						try {
							long offset = promise.get();
							commitOffset(offset, partition);
						} catch (Exception ex) {
							getExceptionHandler().accept(ex);
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
				getExceptionHandler().accept(e);
			} finally {
				consumer.unsubscribe();
				consumer.close();
			}
		}

		private void commitOffset(long offset, TopicPartition partition) {
			if (offset != -1)
				consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(offset)));
		}

		private void seekOffset(Duration pollDuration) {
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
		}

		private Promise<Long, Exception> processBatchRecords(List<ConsumerRecord<Object, Object>> records) {

			long partitionLastOffset = records.get(records.size() - 1).offset();
			var deferred = new CompletableDeferredObject<Message, Exception>();
			var msg = buildMessageForBatch(records);
			publish(msg, deferred);
			return deferred.promise().filterDone(result -> partitionLastOffset);
		}

		private Message buildMessageForBatch(List<ConsumerRecord<Object, Object>> records) {
			var headers = BObject.newDefault();

			var lastRecord = records.get(records.size() - 1);

			populateCommonHeaders(headers, lastRecord);

			var offsets = records.stream().map(record -> record.offset()).toArray(size -> new Long[size]);

			headers.putAny(KafkaConstants.BATCH, true);
			headers.putAny(KafkaConstants.OFFSET, offsets);

			var body = BArray.newFromSequence(records);
			return createMessage(headers, body);
		}

		private Promise<Long, Exception> processSingleRecord(TopicPartition partition,
				List<ConsumerRecord<Object, Object>> records) {
			boolean breakOnFirstError = configuration.isBreakOnFirstError();

			long lastRecord = -1;
			for (var record : records) {
				var msg = buildMessage(record);
				var deferred = new CompletableDeferredObject<Message, Exception>();
				publish(msg, deferred);
				try {
					deferred.promise().get();
					lastRecord = record.offset();
				} catch (Exception ex) {
					ex.printStackTrace();
					if (breakOnFirstError) {
						commitOffset(lastRecord, partition);
						return new SimpleFailurePromise<>(ex);
					}
				}
			}
			return new SimpleDonePromise<>(lastRecord);
		}

		private Message buildMessage(ConsumerRecord<Object, Object> record) {
			var headers = BObject.newDefault();

			populateCommonHeaders(headers, record);

			headers.putAny(KafkaConstants.OFFSET, record.offset());
			if (record.key() != null) {
				headers.putAny(KafkaConstants.KEY, record.key());
			}
			for (Header header : record.headers()) {
				headers.putAny(header.key(), header.value());
			}

			var body = BFactory.DEFAULT.fromAny(record.value());
			return createMessage(headers, body);
		}

		private void populateCommonHeaders(BObject headers, ConsumerRecord<Object, Object> lastRecord) {
			headers.putAny(KafkaConstants.PARTITION, lastRecord.partition());
			headers.putAny(KafkaConstants.TOPIC, lastRecord.topic());
			headers.putAny(KafkaConstants.TIMESTAMP, lastRecord.timestamp());
		}

		private void shutdown() {
			stopped = true;
			if (consumer != null)
				consumer.wakeup();
		}
	}
}
