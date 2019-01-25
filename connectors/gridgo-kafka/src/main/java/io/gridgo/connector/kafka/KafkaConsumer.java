package io.gridgo.connector.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.FormattedMarshallable;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumer extends AbstractConsumer implements FormattedMarshallable {

    private static final int DEFAULT_THREADS = 8;

    private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new ExecutorExecutionStrategy(DEFAULT_THREADS);

    static {
        DEFAULT_EXECUTION_STRATEGY.start();
        Runtime.getRuntime().addShutdownHook(new Thread(DEFAULT_EXECUTION_STRATEGY::stop));
    }

    private final KafkaConfiguration configuration;

    private List<KafkaFetchRecords> tasks;

    @Getter
    private String format;

    public KafkaConsumer(ConnectorContext context, final @NonNull KafkaConfiguration configuration, String format) {
        super(context);
        this.configuration = configuration;
        this.format = format;
    }

    @Override
    protected String generateName() {
        return "consumer.kafka." + configuration.getTopic();
    }

    private Properties getProps() {
        return configuration.createConsumerProperties();
    }

    @Override
    protected void onStart() {
        var consumerExecutionStrategy = getContext().getConsumerExecutionStrategy().orElse(DEFAULT_EXECUTION_STRATEGY);
        consumerExecutionStrategy.start();

        tasks = new ArrayList<>();

        var props = getProps();

        Pattern pattern = null;
        if (configuration.isTopicIsPattern()) {
            pattern = Pattern.compile(configuration.getTopic());
        }

        for (int i = 0; i < configuration.getConsumersCount(); i++) {
            var task = new KafkaFetchRecords(configuration.getTopic(), pattern, i + "", props);
            task.doInit();
            consumerExecutionStrategy.execute(task);
            tasks.add(task);
        }
    }

    @Override
    protected void onStop() {
        for (KafkaFetchRecords task : tasks) {
            task.shutdown();
        }
        var consumerExecutionStrategy = getContext().getConsumerExecutionStrategy().orElse(null);
        if (consumerExecutionStrategy != null)
            consumerExecutionStrategy.stop();
    }

    class KafkaFetchRecords implements Runnable {

        private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> consumer;

        private final String topicName;

        private final Properties kafkaProps;

        private String id;

        private volatile boolean stopped = false;

        private Pattern pattern;

        public KafkaFetchRecords(String topicName, Pattern pattern, String id, Properties kafkaProps) {
            this.topicName = topicName;
            this.pattern = pattern;
            this.id = id;
            this.kafkaProps = kafkaProps;
        }

        private Message buildMessage(ConsumerRecord<Object, Object> record) {
            var headers = BObject.ofEmpty();

            populateCommonHeaders(headers, record);

            headers.putAny(KafkaConstants.OFFSET, record.offset());
            if (record.key() != null) {
                headers.putAny(KafkaConstants.KEY, record.key());
            }

            var isValue = false;
            for (Header header : record.headers()) {
                headers.putAny(header.key(), header.value());
                if (KafkaConstants.IS_VALUE.equals(header.key()))
                    isValue = true;
            }

            var body = isValue ? BElement.ofAny(record.value()) : deserializeWithFormat(record);
            return createMessage(headers, body);
        }

        private BElement deserializeWithFormat(ConsumerRecord<Object, Object> record) {
            var value = record.value();
            return deserialize(value instanceof byte[] ? (byte[]) value : value.toString().getBytes());
        }

        private Message buildMessageForBatch(List<ConsumerRecord<Object, Object>> records) {
            var messages = records.stream().map(this::buildMessage).toArray(size -> new Message[size]);
            var multiPart = new MultipartMessage(messages);

            var headers = multiPart.headers();

            var lastRecord = records.get(records.size() - 1);

            populateCommonHeaders(headers, lastRecord);

            headers.putAny(KafkaConstants.IS_BATCH, true);
            headers.putAny(KafkaConstants.BATCH_SIZE, records.size());
            headers.putAny(KafkaConstants.OFFSET, lastRecord.offset());
            return multiPart;
        }

        private void cleanUpConsumer() {
            try {
                consumer.unsubscribe();
            } finally {
                consumer.close();
            }
        }

        private void commitOffset(long offset, TopicPartition partition, boolean force) {
            if (offset == -1)
                return;
            if (!force && "async".equals(configuration.getCommitType())) {
                consumer.commitAsync(Collections.singletonMap(partition, new OffsetAndMetadata(offset + 1)),
                        (result, ex) -> {
                            if (ex != null) {
                                log.error("Commit failed on topic {} - {}", partition.topic(), partition.partition(),
                                        ex);
                            }
                        });
            } else {
                consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(offset + 1)));
            }
        }

        protected void doInit() {
            ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread()
                      .setContextClassLoader(org.apache.kafka.clients.consumer.KafkaConsumer.class.getClassLoader());
                this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(kafkaProps);
            } finally {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }

        private boolean doRun() {
            boolean reConnect = false;

            var pollDuration = Duration.ofMillis(100);
            var batchProcessing = configuration.isBatchEnabled();

            Thread.currentThread().setName("KAFKA-CONSUMER-" + topicName + "-" + id);

            try {
                subscribeTopics();

                seekOffset(pollDuration);

                while (!stopped && !reConnect && !Thread.currentThread().isInterrupted()) {

                    reConnect = fetchAndProcess(reConnect, pollDuration, batchProcessing);
                }

                if (!reConnect) {
                    commitFinal();
                }
            } catch (WakeupException e) {
                log.debug("WakeupException caught on consumer thread", e);
            } catch (KafkaException e) {
                log.error("KafkaException caught on consumer thread", e);
                reConnect = true;
            } catch (Exception e) {
                log.error("Exception caught on consumer thread", e);
                getContext().getExceptionHandler().accept(e);
            } finally {
                cleanUpConsumer();
            }

            return reConnect;
        }

        private void commitFinal() {
            if (!configuration.isAutoCommitEnable())
                return;
            if ("async".equals(configuration.getAutoCommitOnStop())) {
                consumer.commitAsync();
            } else if ("sync".equals(configuration.getAutoCommitOnStop())) {
                consumer.commitSync();
            }
        }

        private boolean fetchAndProcess(boolean reConnect, Duration pollDuration, boolean batchProcessing) {
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

                long offset = -1;
                try {
                    offset = promise.get();
                } catch (Exception ex) {
                    log.error("Exception caught on processing records", ex);
                    getContext().getExceptionHandler().accept(ex);
                    reConnect = true;
                }
                commitOffset(offset, partition, reConnect);
            }
            return reConnect;
        }

        private void populateCommonHeaders(BObject headers, ConsumerRecord<Object, Object> lastRecord) {
            headers.putAny(KafkaConstants.PARTITION, lastRecord.partition());
            headers.putAny(KafkaConstants.TOPIC, lastRecord.topic());
            headers.putAny(KafkaConstants.TIMESTAMP, lastRecord.timestamp());
        }

        private Promise<Long, Exception> processBatchRecords(List<ConsumerRecord<Object, Object>> records) {

            long partitionLastOffset = records.get(records.size() - 1).offset();
            var deferred = new AsyncDeferredObject<Message, Exception>();
            var msg = buildMessageForBatch(records);
            publish(msg, deferred);
            return deferred.promise().filterDone(result -> partitionLastOffset);
        }

        private Promise<Long, Exception> processSingleRecord(TopicPartition partition,
                List<ConsumerRecord<Object, Object>> records) {
            boolean breakOnFirstError = configuration.isBreakOnFirstError();

            long lastRecord = -1;
            for (var record : records) {
                var msg = buildMessage(record);
                var deferred = new AsyncDeferredObject<Message, Exception>();
                publish(msg, deferred);
                try {
                    deferred.promise().get();
                    lastRecord = record.offset();
                } catch (Exception ex) {
                    log.error("Exception caught while processing ConsumerRecord", ex);
                    if (breakOnFirstError) {
                        commitOffset(lastRecord, partition, true);
                        return Promise.ofCause(ex);
                    }
                }
            }
            return Promise.of(lastRecord);
        }

        @Override
        public void run() {
            boolean first = true;
            boolean reConnect = true;

            while (reConnect) {
                try {
                    if (!first) {
                        // re-initialize on re-connect so we have a fresh consumer
                        doInit();
                    }
                } catch (Throwable e) {
                    log.warn("Exception caught when initializing KafkaConsumer", e);
                }

                if (!first) {
                    // skip one poll timeout before trying again
                    long delay = configuration.getPollTimeoutMs();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                first = false;

                // doRun keeps running until we either shutdown or is told to re-connect
                reConnect = doRun();
            }
        }

        private void seekOffset(Duration pollDuration) {
            if (configuration.getSeekTo() == null)
                return;
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

        private void shutdown() {
            stopped = true;
            if (consumer != null)
                consumer.wakeup();
        }

        private void subscribeTopics() {
            if (configuration.isTopicIsPattern()) {
                consumer.subscribe(pattern);
            } else {
                consumer.subscribe(Arrays.asList(topicName.split(",")));
            }
        }
    }
}
