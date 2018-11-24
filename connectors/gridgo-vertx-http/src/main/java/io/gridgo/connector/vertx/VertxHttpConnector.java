package io.gridgo.connector.vertx;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;

@ConnectorEndpoint(scheme = "vertx", syntax = "http://{host}:{port}/[{path}]")
public class VertxHttpConnector extends AbstractConnector {

	@Override
	public void onInit() {
		String path = getPath();
		String method = getParam(VertxHttpConstants.PARAM_METHOD);
		String format = getParam(VertxHttpConstants.PARAM_FORMAT);
		String vertxBean = getParam(VertxHttpConstants.PARAM_VERTX_BEAN);
		Vertx vertx = null;
		if (vertxBean != null) {
			vertx = getContext().getRegistry().lookupMandatory(vertxBean, Vertx.class);
		}
		var vertxOptions = buildVertxOptions();
		var httpOptions = buildHttpServerOptions();
		var vertxConsumer = new VertxHttpConsumer(getContext(), vertx, vertxOptions, httpOptions, path, method, format,
				getConnectorConfig().getParameters());
		this.consumer = Optional.of(vertxConsumer);
	}

	private VertxOptions buildVertxOptions() {
		String workerPoolSize = getParam(VertxHttpConstants.PARAM_WORKER_POOL_SIZE);
		String eventLoopPoolSize = getParam(VertxHttpConstants.PARAM_EVENT_LOOP_POOL_SIZE);
		var options = new VertxOptions();
		if (workerPoolSize != null)
			options.setWorkerPoolSize(Integer.parseInt(workerPoolSize));
		if (eventLoopPoolSize != null)
			options.setEventLoopPoolSize(Integer.parseInt(eventLoopPoolSize));
		return options;
	}

	private HttpServerOptions buildHttpServerOptions() {
		String compressionLevel = getParam(VertxHttpConstants.PARAM_COMPRESSION_LEVEL);
		String compressionSupported = getParam(VertxHttpConstants.PARAM_COMPRESSION_SUPPORTED);
		boolean useAlpn = Boolean.valueOf(getParam(VertxHttpConstants.PARAM_USE_ALPN, "false"));
		boolean ssl = Boolean.valueOf(getParam(VertxHttpConstants.PARAM_SSL, "false"));
		var clientAuth = ClientAuth.valueOf(getParam(VertxHttpConstants.PARAM_CLIENT_AUTH, ClientAuth.NONE.toString()));
		String keyStorePath = getParam(VertxHttpConstants.PARAM_KEY_STORE_PATH);
		String keyStorePassword = getParam(VertxHttpConstants.PARAM_KEY_STORE_PASSWORD);
		var keyStoreOptions = keyStorePath != null
				? new JksOptions().setPath(keyStorePath).setPassword(keyStorePassword)
				: null;
		var options = new HttpServerOptions().setUseAlpn(useAlpn).setSsl(ssl).setClientAuth(clientAuth)
				.setHost(getPlaceholder(VertxHttpConstants.PLACEHOLDER_HOST))
				.setPort(Integer.parseInt(getPlaceholder(VertxHttpConstants.PLACEHOLDER_PORT)))
				.setKeyStoreOptions(keyStoreOptions);
		if (compressionLevel != null)
			options.setCompressionLevel(Integer.parseInt(compressionLevel));
		if (compressionSupported != null)
			options.setCompressionSupported(Boolean.valueOf(compressionSupported));
		return options;
	}

	private String getPath() {
		String path = getPlaceholder(VertxHttpConstants.PLACEHOLDER_PATH);
		if (path != null)
			path = "/" + path;
		return path;
	}
}
