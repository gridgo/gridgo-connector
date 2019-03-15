package io.gridgo.connector.kafka;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import lombok.Data;

@Data
public class KafkaConfiguration {

    private static <T> void addPropertyIfNotNull(Properties props, String key, T value) {
        if (value != null) {
            // Kafka expects all properties as String
            props.put(key, value.toString());
        }
    }

    // common
    private String topic;

    private String brokers;

    private String clientId;

    private String groupId;

    private String transactionId;

    private int consumersCount = 1;

    private String interceptorClasses;

    private String keyDeserializer = KafkaConstants.KAFKA_DEFAULT_DESERIALIZER;

    private String valueDeserializer = KafkaConstants.KAFKA_DEFAULT_DESERIALIZER;

    private int fetchMinBytes = 1;

    private int fetchMaxBytes = 50 * 1024 * 1024;

    private int heartbeatIntervalMs = 3000;

    private int maxPartitionFetchBytes = 1048576;

    private int sessionTimeoutMs = 10000;

    private Integer maxPollRecords;

    private Long pollTimeoutMs = 5000L;

    private Long maxPollIntervalMs;

    private String autoOffsetReset = "latest";

    private String partitionAssignor = KafkaConstants.PARTITIONER_RANGE_ASSIGNOR;

    private int consumerRequestTimeoutMs = 40000;

    private int autoCommitIntervalMs = 5000;

    private boolean checkCrcs = true;

    private int fetchWaitMaxMs = 500;

    private String seekTo;

    private boolean batchEnabled = false;

    private boolean breakOnFirstError = true;

    // consumer
    private boolean autoCommitEnable = true;

    private String autoCommitOnStop = "sync";

    private String commitType = "sync";

    // producer

    private boolean topicIsPattern = false;

    private String partitioner = KafkaConstants.KAFKA_DEFAULT_PARTITIONER;

    private int retryBackoffMs = 100;

    private String serializerClass = KafkaConstants.KAFKA_DEFAULT_SERIALIZER;

    private String keySerializerClass = KafkaConstants.KAFKA_DEFAULT_SERIALIZER;

    private String acks = "1";

    private int bufferMemorySize = 33554432;

    private String compressionCodec = "none";

    private Integer retries = null;

    private int producerBatchSize = 16384;

    private int connectionMaxIdleMs = 540000;

    private int lingerMs = 0;

    private int maxBlockMs = 60000;

    private int maxRequestSize = 1048576;

    private int receiveBufferBytes = 65536;

    private int requestTimeoutMs = 305000;

    private int sendBufferBytes = 131072;

    private boolean recordMetadata = true;

    private int maxInFlightRequest = 5;

    private int metadataMaxAgeMs = 300000;

    private String metricReporters;

    private int noOfMetricsSample = 2;

    private int metricsSampleWindowMs = 30000;

    private int reconnectBackoffMs = 50;

    private boolean enableIdempotence;

    private int reconnectBackoffMaxMs = 1000;

    public Properties createConsumerProperties() {
        var props = new Properties();

        addPropertyIfNotNull(props, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBrokers());
        addPropertyIfNotNull(props, ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        addPropertyIfNotNull(props, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, getKeyDeserializer());
        addPropertyIfNotNull(props, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, getValueDeserializer());
        addPropertyIfNotNull(props, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, getFetchMinBytes());
        addPropertyIfNotNull(props, ConsumerConfig.FETCH_MAX_BYTES_CONFIG, getFetchMaxBytes());
        addPropertyIfNotNull(props, ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, getHeartbeatIntervalMs());
        addPropertyIfNotNull(props, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, getMaxPartitionFetchBytes());
        addPropertyIfNotNull(props, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, getSessionTimeoutMs());
        addPropertyIfNotNull(props, ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, getMaxPollIntervalMs());
        addPropertyIfNotNull(props, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, getMaxPollRecords());
        addPropertyIfNotNull(props, ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, getInterceptorClasses());
        addPropertyIfNotNull(props, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, getAutoOffsetReset());
        addPropertyIfNotNull(props, ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, getConnectionMaxIdleMs());
        addPropertyIfNotNull(props, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isAutoCommitEnable());
        addPropertyIfNotNull(props, ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, getPartitionAssignor());
        addPropertyIfNotNull(props, ConsumerConfig.RECEIVE_BUFFER_CONFIG, getReceiveBufferBytes());
        addPropertyIfNotNull(props, ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, getConsumerRequestTimeoutMs());
        addPropertyIfNotNull(props, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, getAutoCommitIntervalMs());
        addPropertyIfNotNull(props, ConsumerConfig.CHECK_CRCS_CONFIG, isCheckCrcs());
        addPropertyIfNotNull(props, ConsumerConfig.CLIENT_ID_CONFIG, getClientId());
        addPropertyIfNotNull(props, ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, getFetchWaitMaxMs());
        addPropertyIfNotNull(props, ConsumerConfig.METADATA_MAX_AGE_CONFIG, getMetadataMaxAgeMs());
        addPropertyIfNotNull(props, ConsumerConfig.METRIC_REPORTER_CLASSES_CONFIG, getMetricReporters());
        addPropertyIfNotNull(props, ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, getNoOfMetricsSample());
        addPropertyIfNotNull(props, ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, getMetricsSampleWindowMs());
        addPropertyIfNotNull(props, ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, getReconnectBackoffMs());
        addPropertyIfNotNull(props, ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, getRetryBackoffMs());
        addPropertyIfNotNull(props, ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, getReconnectBackoffMaxMs());

        return props;
    }

    public Properties createProducerProperties() {
        var props = new Properties();

        addPropertyIfNotNull(props, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBrokers());
        addPropertyIfNotNull(props, ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        addPropertyIfNotNull(props, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, getKeySerializerClass());
        addPropertyIfNotNull(props, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, getSerializerClass());
        addPropertyIfNotNull(props, ProducerConfig.ACKS_CONFIG, getAcks());
        addPropertyIfNotNull(props, ProducerConfig.BUFFER_MEMORY_CONFIG, getBufferMemorySize());
        addPropertyIfNotNull(props, ProducerConfig.COMPRESSION_TYPE_CONFIG, getCompressionCodec());
        addPropertyIfNotNull(props, ProducerConfig.RETRIES_CONFIG, getRetries());
        addPropertyIfNotNull(props, ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, getInterceptorClasses());
        addPropertyIfNotNull(props, ProducerConfig.SEND_BUFFER_CONFIG, getRetries());
        addPropertyIfNotNull(props, ProducerConfig.BATCH_SIZE_CONFIG, getProducerBatchSize());
        addPropertyIfNotNull(props, ProducerConfig.CLIENT_ID_CONFIG, getClientId());
        addPropertyIfNotNull(props, ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, getConnectionMaxIdleMs());
        addPropertyIfNotNull(props, ProducerConfig.LINGER_MS_CONFIG, getLingerMs());
        addPropertyIfNotNull(props, ProducerConfig.MAX_BLOCK_MS_CONFIG, getMaxBlockMs());
        addPropertyIfNotNull(props, ProducerConfig.MAX_REQUEST_SIZE_CONFIG, getMaxRequestSize());
        addPropertyIfNotNull(props, ProducerConfig.PARTITIONER_CLASS_CONFIG, getPartitioner());
        addPropertyIfNotNull(props, ProducerConfig.RECEIVE_BUFFER_CONFIG, getReceiveBufferBytes());
        addPropertyIfNotNull(props, ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, getRequestTimeoutMs());
        addPropertyIfNotNull(props, ProducerConfig.SEND_BUFFER_CONFIG, getSendBufferBytes());
        addPropertyIfNotNull(props, ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, getMaxInFlightRequest());
        addPropertyIfNotNull(props, ProducerConfig.METADATA_MAX_AGE_CONFIG, getMetadataMaxAgeMs());
        addPropertyIfNotNull(props, ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, getMetricReporters());
        addPropertyIfNotNull(props, ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, getNoOfMetricsSample());
        addPropertyIfNotNull(props, ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, getMetricsSampleWindowMs());
        addPropertyIfNotNull(props, ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, getReconnectBackoffMs());
        addPropertyIfNotNull(props, ProducerConfig.RETRY_BACKOFF_MS_CONFIG, getRetryBackoffMs());
        addPropertyIfNotNull(props, ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, isEnableIdempotence());
        addPropertyIfNotNull(props, ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, getReconnectBackoffMaxMs());
        addPropertyIfNotNull(props, ProducerConfig.TRANSACTIONAL_ID_CONFIG, getTransactionId());

        return props;
    }
}
