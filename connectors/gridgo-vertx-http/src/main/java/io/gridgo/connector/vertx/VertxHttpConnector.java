package io.gridgo.connector.vertx;

import java.util.Optional;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import lombok.Getter;

@ConnectorEndpoint(scheme = "vertx", syntax = "http://{host}:{port}/{path}")
public class VertxHttpConnector implements Connector {

	@Getter
	private ConnectorConfig connectorConfig;

	private Optional<Consumer> consumer;

	@Override
	public Connector initialize(ConnectorConfig config) {
		this.connectorConfig = config;
		String path = config.getPlaceholders().getProperty("path");
		if (path != null)
			path = "/" + path;
		String method = getParam(config, "method");
		String format = getParam(config, "format");
		VertxOptions vertxOptions = buildVertxOptions(config);
		HttpServerOptions httpOptions = buildHttpServerOptions(config);
		this.consumer = Optional.of(new VertxHttpConsumer(vertxOptions, httpOptions, path, method, format));
		return this;
	}

	private VertxOptions buildVertxOptions(ConnectorConfig config) {
		String workerPoolSize = getParam(config, "workerPoolSize");
		String eventLoopPoolSize = getParam(config, "eventLoopPoolSize");
		VertxOptions options = new VertxOptions();
		if (workerPoolSize != null)
			options.setWorkerPoolSize(Integer.parseInt(workerPoolSize));
		if (eventLoopPoolSize != null)
			options.setEventLoopPoolSize(Integer.parseInt(eventLoopPoolSize));
		return options;
	}

	private HttpServerOptions buildHttpServerOptions(ConnectorConfig config) {
		boolean useAlpn = Boolean.valueOf(getParam(config, "useAlpn", "false"));
		boolean ssl = Boolean.valueOf(getParam(config, "ssl", "false"));
		ClientAuth clientAuth = ClientAuth.valueOf(getParam(config, "clientAuth", ClientAuth.NONE.toString()));
		String keyStorePath = getParam(config, "keyStorePath");
		String keyStorePassword = getParam(config, "keyStorePassword");
		JksOptions keyStoreOptions = keyStorePath != null
				? new JksOptions().setPath(keyStorePath).setPassword(keyStorePassword)
				: null;
		return new HttpServerOptions().setUseAlpn(useAlpn).setSsl(ssl).setClientAuth(clientAuth)
				.setHost(config.getPlaceholders().getProperty("host"))
				.setPort(Integer.parseInt(config.getPlaceholders().getProperty("port")))
				.setKeyStoreOptions(keyStoreOptions);
	}

	private String getParam(ConnectorConfig config, String name) {
		Object value = config.getParameters().get(name);
		return value != null ? value.toString() : null;
	}

	private String getParam(ConnectorConfig config, String name, String defaultValue) {
		Object value = config.getParameters().getOrDefault(name, defaultValue);
		return value != null ? value.toString() : null;
	}

	public void start() {
		if (consumer.isPresent())
			consumer.get().start();
	}

	public void stop() {
		if (consumer.isPresent())
			consumer.get().stop();
	}

	@Override
	public Optional<Producer> getProducer() {
		return Optional.empty();
	}

	@Override
	public Optional<Consumer> getConsumer() {
		return consumer;
	}
}
