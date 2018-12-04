package io.gridgo.connector.jetty;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;

public interface JettyConsumer extends Consumer, HasResponder, FailureHandlerAware<JettyConsumer> {

}
