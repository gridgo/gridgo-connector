package io.gridgo.connector.scheduler;

import java.util.Optional;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "scheduler", syntax = "{name}")
public class SchedulerConnector extends AbstractConnector {

    @Override
    protected void onInit() {
        var name = getPlaceholder("name");
        var params = getConnectorConfig().getParameters();
        this.consumer = Optional.of(new SchedulerConsumer(getContext(), name, BObject.of(params)));
    }
}
