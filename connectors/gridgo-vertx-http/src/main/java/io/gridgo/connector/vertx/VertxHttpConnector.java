package io.gridgo.connector.vertx;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

@ConnectorEndpoint(scheme = "vertx", syntax = "http://{host}:{port}/[{path}]")
public class VertxHttpConnector extends AbstractConnector {

	@Override
	public void onInit() {
		var config = getConnectorConfig();
		String path = config.getPlaceholders().getProperty(VertxHttpConstants.PLACEHOLDER_PATH);
		if (path != null)
			path = "/" + path;
		String method = getParam(config, VertxHttpConstants.PARAM_METHOD);
		String format = getParam(config, VertxHttpConstants.PARAM_FORMAT);
		String vertxBean = getParam(config, VertxHttpConstants.PARAM_VERTX_BEAN);
		var vertxOptions = buildVertxOptions(config);
		Vertx vertx = null;
		if (vertxBean != null) {
			vertx = getContext().getRegistry().lookup(vertxBean, Vertx.class);
		}
		var httpOptions = buildHttpServerOptions(config);
		this.consumer = Optional
				.of(new VertxHttpConsumer(getContext(), vertx, vertxOptions, httpOptions, path, method, format));
	}

	private VertxOptions buildVertxOptions(ConnectorConfig config) {
		String workerPoolSize = getParam(config, VertxHttpConstants.PARAM_WORKER_POOL_SIZE);
		String eventLoopPoolSize = getParam(config, VertxHttpConstants.PARAM_EVENT_LOOP_POOL_SIZE);
		var options = new VertxOptions();
		if (workerPoolSize != null)
			options.setWorkerPoolSize(Integer.parseInt(workerPoolSize));
		if (eventLoopPoolSize != null)
			options.setEventLoopPoolSize(Integer.parseInt(eventLoopPoolSize));
		return options;
	}

	private HttpServerOptions buildHttpServerOptions(ConnectorConfig config) {
		String compressionLevel = getParam(config, VertxHttpConstants.PARAM_COMPRESSION_LEVEL);
		String compressionSupported = getParam(config, VertxHttpConstants.PARAM_COMPRESSION_SUPPORTED);
		boolean useAlpn = Boolean.valueOf(getParam(config, VertxHttpConstants.PARAM_USE_ALPN, "false"));
		boolean ssl = Boolean.valueOf(getParam(config, VertxHttpConstants.PARAM_SSL, "false"));
		var clientAuth = ClientAuth
				.valueOf(getParam(config, VertxHttpConstants.PARAM_CLIENT_AUTH, ClientAuth.NONE.toString()));
		String keyStorePath = getParam(config, VertxHttpConstants.PARAM_KEY_STORE_PATH);
		String keyStorePassword = getParam(config, VertxHttpConstants.PARAM_KEY_STORE_PASSWORD);
		var keyStoreOptions = keyStorePath != null
				? new JksOptions().setPath(keyStorePath).setPassword(keyStorePassword)
				: null;
		var options = new HttpServerOptions().setUseAlpn(useAlpn).setSsl(ssl).setClientAuth(clientAuth)
				.setHost(config.getPlaceholders().getProperty(VertxHttpConstants.PLACEHOLDER_HOST))
				.setPort(Integer.parseInt(config.getPlaceholders().getProperty(VertxHttpConstants.PLACEHOLDER_PORT)))
				.setKeyStoreOptions(keyStoreOptions);
		if (compressionLevel != null)
			options.setCompressionLevel(Integer.parseInt(compressionLevel));
		if (compressionSupported != null)
			options.setCompressionSupported(Boolean.valueOf(compressionSupported));
		return options;
	}
}
