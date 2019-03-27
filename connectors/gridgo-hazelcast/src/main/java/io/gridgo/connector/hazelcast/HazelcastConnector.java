package io.gridgo.connector.hazelcast;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "hazelcast", syntax = "{type}://{name}")
public class HazelcastConnector extends AbstractConnector {

    @Override
    protected void onInit() {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
