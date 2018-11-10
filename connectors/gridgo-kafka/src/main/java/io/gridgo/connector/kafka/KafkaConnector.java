package io.gridgo.connector.kafka;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.utils.ObjectUtils;

@ConnectorEndpoint(scheme = "kafka", syntax = "{topic}")
public class KafkaConnector extends AbstractConnector {

	protected void onInit() {
		var config = getConnectorConfig();
		String mode = getParam(config, "mode", "both");
		boolean createConsumer = true, createProducer = true;
		if (mode.equals("consumer")) {
			createProducer = false;
		} else if (mode.equals("producer")) {
			createConsumer = false;
		}
		var kafkaConfig = createKafkaConfig(config);
		if (createConsumer)
			consumer = Optional.of(new KafkaConsumer(kafkaConfig));
		if (createProducer)
			producer = Optional.of(new KafkaProducer(kafkaConfig));
	}

	private KafkaConfiguration createKafkaConfig(ConnectorConfig config) {
		var kafkaConfig = new KafkaConfiguration();
		ObjectUtils.assembleFromMap(KafkaConfiguration.class, kafkaConfig, config.getParameters());
		kafkaConfig.setTopic(config.getPlaceholders().getProperty("topic"));
		return kafkaConfig;
	}

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
