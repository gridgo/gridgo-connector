package io.gridgo.connector.play;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_FORMAT;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_METHOD;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "play", syntax = ":[{engine}:]//{host}:{port}/[{path}]")
public class PlayConnector extends AbstractConnector {

    @Override
    public void onInit() {
        var method = getParam(PARAM_METHOD);
        var format = getParam(PARAM_FORMAT);
        this.consumer = Optional.of(new PlayConsumer(getContext()));
    }
}
