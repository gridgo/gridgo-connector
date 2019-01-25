package io.gridgo.connector.kafka;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
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
import io.gridgo.connector.support.FormattedMarshallable;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.transaction.Transaction;
import io.gridgo.connector.support.transaction.TransactionalComponent;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;
import lombok.Getter;

public class KafkaProducer extends AbstractProducer
        implements TransactionalComponent, Transaction, FormattedMarshallable {

    private KafkaConfiguration configuration;

    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer;

    private String[] topics;

    @Getter
    private String format;

    public KafkaProducer(ConnectorContext context, KafkaConfiguration configuration, String format) {
        super(context);
        this.configuration = configuration;
        this.topics = configuration.getTopic().split(",");
        this.format = format;
    }

    private void ack(Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
        var msg = buildAckMessage(metadata);
        ack(deferred, msg, exception);
    }

    private Message buildAckMessage(RecordMetadata metadata) {
        if (metadata == null)
            return null;
        var headers = BObject.ofEmpty().setAny(KafkaConstants.IS_ACK_MSG, "true")
                             .setAny(KafkaConstants.TIMESTAMP, metadata.timestamp())
                             .setAny(KafkaConstants.OFFSET, metadata.offset())
                             .setAny(KafkaConstants.PARTITION, metadata.partition())
                             .setAny(KafkaConstants.TOPIC, metadata.topic());
        return createMessage(headers, BValue.ofEmpty());
    }

    private ProducerRecord<Object, Object> buildProducerRecord(String topic, Message message) {
        var headers = message.headers();

        var partitionValue = headers.getValue(KafkaConstants.PARTITION);
        Integer partition = partitionValue != null ? partitionValue.getInteger() : null;
        var timestampValue = headers.getValue(KafkaConstants.TIMESTAMP);
        Long timestamp = timestampValue != null ? timestampValue.getLong() : null;
        var keyValue = headers.getValue(KafkaConstants.KEY);
        Object key = keyValue != null ? keyValue.getData() : null;
        var body = message.body();
        var record = new ProducerRecord<Object, Object>(topic, partition, timestamp, key, convert(body));
        if (body != null && body.isValue()) {
            record.headers().add(KafkaConstants.IS_VALUE, new byte[] { 1 });
        }

        for (var header : headers.entrySet()) {
            if (header.getValue().isValue()) {
                record.headers().add(header.getKey(), header.getValue().asValue().toBytes());
            }
        }

        return record;
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        throw new UnsupportedOperationException();
    }

    private Object convert(BElement body) {
        if (body == null || body.isNullValue())
            return null;
        if (body.isValue())
            return body.asValue().getData();
        if ("raw".equals(format))
            return serialize(body);
        return new String(serialize(body));
    }

    public Message convertJoinedResult(JoinedResults<Message> results) {
        return new MultipartMessage(results);
    }

    @Override
    protected String generateName() {
        return "producer.kafka." + configuration.getTopic();
    }

    private Properties getProps() {
        return configuration.createProducerProperties();
    }

    @Override
    public boolean isCallSupported() {
        return false;
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
            if (configuration.getTransactionId() != null && !configuration.getTransactionId().isBlank())
                this.producer.initTransactions();
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
        }
    }

    @Override
    protected void onStop() {
        if (this.producer != null)
            this.producer.close();
    }

    @Override
    public void send(Message message) {
        sendAllTopics(message, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var promises = sendAllTopics(message, true);
        return promises.size() == 1 ? promises.get(0)
                : JoinedPromise.from(promises).filterDone(this::convertJoinedResult);
    }

    private ArrayList<Promise<Message, Exception>> sendAllTopics(Message message, boolean ack) {
        var promises = new ArrayList<Promise<Message, Exception>>();
        for (var topic : topics) {
            if (!ack) {
                sendSingle(message, topic, null);
            } else {
                var deferred = new CompletableDeferredObject<Message, Exception>();
                sendSingle(message, topic, (metadata, ex) -> ack(deferred, metadata, ex));
                promises.add(deferred);
            }
        }
        return promises;
    }

    private void sendSingle(Message message, String topic, Callback callback) {
        var record = buildProducerRecord(topic, message);
        this.producer.send(record, callback);
    }

    @Override
    public Promise<Transaction, Exception> createTransaction() {
        try {
            this.producer.beginTransaction();
            return Promise.of(this);
        } catch (Exception ex) {
            return Promise.ofCause(ex);
        }
    }

    @Override
    public Promise<Message, Exception> commit() {
        this.producer.commitTransaction();
        return Promise.of(null);
    }

    @Override
    public Promise<Message, Exception> rollback() {
        this.producer.abortTransaction();
        return Promise.of(null);
    }
}
