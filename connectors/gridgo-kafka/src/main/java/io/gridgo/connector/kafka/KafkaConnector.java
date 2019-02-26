package io.gridgo.connector.kafka;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.utils.ObjectUtils;

@ConnectorEndpoint(scheme = "kafka", syntax = "{topic}")
public class KafkaConnector extends AbstractConnector {

    private KafkaConfiguration createKafkaConfig(ConnectorConfig config) {
        var kafkaConfig = new KafkaConfiguration();
        ObjectUtils.assembleFromMap(KafkaConfiguration.class, kafkaConfig, config.getParameters());
        kafkaConfig.setTopic(getPlaceholder(KafkaConstants.PLACEHOLDER_TOPIC));
        return kafkaConfig;
    }

    protected void onInit() {
        var config = getConnectorConfig();
        String mode = getParam(KafkaConstants.PARAM_MODE, KafkaConstants.MODE_BOTH);
        boolean createConsumer = true, createProducer = true;
        if (mode.equals(KafkaConstants.MODE_CONSUMER)) {
            createProducer = false;
        } else if (mode.equals(KafkaConstants.MODE_PRODUCER)) {
            createConsumer = false;
        }
        var format = getParam("format");
        var kafkaConfig = createKafkaConfig(config);
        if (createConsumer)
            consumer = Optional.of(new KafkaConsumer(getContext(), kafkaConfig, format));
        if (createProducer)
            producer = Optional.of(new KafkaProducer(getContext(), kafkaConfig, format));
    }
}
