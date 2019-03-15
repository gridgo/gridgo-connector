package io.gridgo.connector.kafka;

public class KafkaConstants {

    public static final String PLACEHOLDER_TOPIC = "topic";

    public static final String PARAM_MODE = "mode";

    public static final String MODE_PRODUCER = "producer";

    public static final String MODE_CONSUMER = "consumer";

    public static final String MODE_BOTH = "both";

    public static final String LAST_RECORD_BEFORE_COMMIT = "kafka.LAST_RECORD_BEFORE_COMMIT";

    public static final String MANUAL_COMMIT = "kafka.MANUAL_COMMIT";

    public static final String KAFKA_DEFAULT_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    public static final String KAFKA_DEFAULT_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";

    public static final String PARTITIONER_RANGE_ASSIGNOR = "org.apache.kafka.clients.consumer.RangeAssignor";

    public static final String KAFKA_DEFAULT_PARTITIONER = "org.apache.kafka.clients.producer.internals.DefaultPartitioner";

    public static final String KEY = "kafka.KEY";

    public static final String PARTITION = "kafka.PARTITION";

    public static final String TOPIC = "kafka.TOPIC";

    public static final String OFFSET = "kafka.OFFSET";

    public static final String TIMESTAMP = "kafka.TIMESTAMP";

    public static final String RECORD = "kafka.RECORD";

    public static final String IS_BATCH = "isBatch";

    public static final String BATCH_SIZE = "batchSize";

    public static final String IS_ACK_MSG = "kafka.IS_ACK_MSG";

    public static final String IS_VALUE = "kafka.IS_VALUE";
}
