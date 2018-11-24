package io.gridgo.connector.jetty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;

public interface JettyConsumer extends Consumer, HasResponder, FailureHandlerAware<JettyConsumer> {

	public static final Set<String> HTTP_SERVER_TYPES = new HashSet<>(Arrays.asList("http", "http2"));
}
