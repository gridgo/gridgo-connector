package io.gridgo.connector.kafka;

public class KafkaConstants {

	public static final String LAST_RECORD_BEFORE_COMMIT = "kafka.LAST_RECORD_BEFORE_COMMIT";

	public static final String MANUAL_COMMIT = "kafka.MANUAL_COMMIT";
	
	public static final String KAFKA_DEFAULT_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	
	public static final String KAFKA_DEFAULT_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
	
	public static final String PARTITIONER_RANGE_ASSIGNOR = "org.apache.kafka.clients.consumer.RangeAssignor";
	
	public static final String KAFKA_DEFAULT_PARTITIONER = "org.apache.kafka.clients.producer.internals.DefaultPartitioner";

	public static final String KEY = null;

	public static final String PARTITION = null;

	public static final String TOPIC = null;

	public static final String OFFSET = null;

	public static final String TIMESTAMP = null;
}
