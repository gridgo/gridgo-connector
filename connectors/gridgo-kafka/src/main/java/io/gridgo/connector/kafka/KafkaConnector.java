package io.gridgo.connector.kafka;

import java.beans.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;

@ConnectorEndpoint(scheme = "kafka", syntax = "{topic}")
public class KafkaConnector extends AbstractConnector {

	protected void onInit() {
		ConnectorConfig config = getConnectorConfig();
		String mode = getParam(config, "mode", "both");
		boolean createConsumer = true, createProducer = true;
		if (mode.equals("consumer")) {
			createProducer = false;
		} else if (mode.equals("producer")) {
			createConsumer = false;
		}
		KafkaConfiguration kafkaConfig = createKafkaConfig(config);
		if (createConsumer)
			consumer = Optional.of(new KafkaConsumer(kafkaConfig));
		if (createProducer)
			producer = Optional.of(new KafkaProducer(kafkaConfig));
	}

	private KafkaConfiguration createKafkaConfig(ConnectorConfig config) {
		KafkaConfiguration kafkaConfig = new KafkaConfiguration();
		Statement stmt;
		Map<String, Class<?>> fieldMap = Arrays.stream(KafkaConfiguration.class.getDeclaredFields())
				.collect(Collectors.toMap(field -> field.getName(), field -> field.getType()));
		for (String attr : config.getParameters().keySet()) {
			if (!fieldMap.containsKey(attr))
				continue;
			Object value = convertValue(config.getParameters().get(attr), fieldMap.get(attr));
			String setter = "set" + attr.substring(0, 1).toUpperCase() + attr.substring(1);
			stmt = new Statement(kafkaConfig, setter, new Object[] { value });
			try {
				stmt.execute();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		kafkaConfig.setTopic(config.getPlaceholders().getProperty("topic"));
		return kafkaConfig;
	}

	private Object convertValue(Object value, Class<?> type) {
		if (value == null)
			return null;
		if (type == String.class)
			return value.toString();
		if (type == int.class || type == Integer.class)
			return Integer.parseInt(value.toString());
		if (type == long.class || type == Long.class)
			return Long.parseLong(value.toString());
		if (type == boolean.class || type == Boolean.class)
			return Boolean.valueOf(value.toString());
		return value;
	}

	@Override
	protected void onStart() {
		if (consumer.isPresent())
			consumer.get().start();
		if (producer.isPresent())
			producer.get().start();
	}

	@Override
	protected void onStop() {
		if (consumer.isPresent())
			consumer.get().stop();
		if (producer.isPresent())
			producer.get().stop();
	}
}
