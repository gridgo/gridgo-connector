package io.gridgo.connector.vertx;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_COMPRESSION_SUPPORTED;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_FORMAT;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_METHOD;
import static io.gridgo.connector.vertx.VertxHttpConstants.ACCEPT_BACKLOG;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_CLIENT_AUTH;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_COMPRESSION_LEVEL;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_EVENT_LOOP_POOL_SIZE;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_KEY_STORE_PASSWD;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_KEY_STORE_PATH;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_SSL;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_USE_ALPN;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_VERTX_BEAN;
import static io.gridgo.connector.vertx.VertxHttpConstants.PARAM_WORKER_POOL_SIZE;
import static io.gridgo.connector.vertx.VertxHttpConstants.PEM_CERT_PATH;
import static io.gridgo.connector.vertx.VertxHttpConstants.PEM_KEY_PATH;
import static io.gridgo.connector.vertx.VertxHttpConstants.PLACEHOLDER_HOST;
import static io.gridgo.connector.vertx.VertxHttpConstants.PLACEHOLDER_PORT;

import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;

@ConnectorEndpoint(scheme = "vertx", syntax = "http://{host}:{port}/[{path}]")
public class VertxHttpConnector extends AbstractConnector {

    private HttpServerOptions buildHttpServerOptions() {
        var acceptBacklog = getParam(ACCEPT_BACKLOG);
        var compressionLevel = getParam(PARAM_COMPRESSION_LEVEL);
        var compressionSupported = getParam(PARAM_COMPRESSION_SUPPORTED);
        var useAlpn = Boolean.valueOf(getParam(PARAM_USE_ALPN, "false"));
        var ssl = Boolean.valueOf(getParam(PARAM_SSL, "false"));
        var clientAuth = ClientAuth.valueOf(getParam(PARAM_CLIENT_AUTH, ClientAuth.NONE.toString()));
        var keyStorePath = getParam(PARAM_KEY_STORE_PATH);
        var keyStorePassword = getParam(PARAM_KEY_STORE_PASSWD);
        var keyStoreOptions = keyStorePath != null ? new JksOptions().setPath(keyStorePath).setPassword(keyStorePassword) : null;
        var options = new HttpServerOptions().setUseAlpn(useAlpn).setSsl(ssl).setClientAuth(clientAuth).setHost(getPlaceholder(PLACEHOLDER_HOST))
                                             .setPort(Integer.parseInt(getPlaceholder(PLACEHOLDER_PORT))).setKeyStoreOptions(keyStoreOptions);
        if (ssl) {
            options.setKeyCertOptions(new PemKeyCertOptions().setCertPath(getParam(PEM_CERT_PATH)) //
                                                             .setKeyPath(getParam(PEM_KEY_PATH)));
        }
        if (acceptBacklog != null)
            options.setAcceptBacklog(Integer.parseInt(acceptBacklog));
        if (compressionLevel != null)
            options.setCompressionLevel(Integer.parseInt(compressionLevel));
        if (compressionSupported != null)
            options.setCompressionSupported(Boolean.valueOf(compressionSupported));
        return options;
    }

    private VertxOptions buildVertxOptions() {
        String workerPoolSize = getParam(PARAM_WORKER_POOL_SIZE);
        String eventLoopPoolSize = getParam(PARAM_EVENT_LOOP_POOL_SIZE);
        var options = new VertxOptions();
        if (workerPoolSize != null)
            options.setWorkerPoolSize(Integer.parseInt(workerPoolSize));
        if (eventLoopPoolSize != null)
            options.setEventLoopPoolSize(Integer.parseInt(eventLoopPoolSize));
        return options;
    }

    private String getPath() {
        String path = getPlaceholder(VertxHttpConstants.PLACEHOLDER_PATH);
        if (path != null)
            path = "/" + path;
        return path;
    }

    @Override
    public void onInit() {
        String path = getPath();
        String method = getParam(PARAM_METHOD);
        String format = getParam(PARAM_FORMAT);
        String vertxBean = getParam(PARAM_VERTX_BEAN);
        Vertx vertx = null;
        if (vertxBean != null) {
            vertx = getContext().getRegistry().lookupMandatory(vertxBean, Vertx.class);
        }
        var vertxOptions = buildVertxOptions();
        var httpOptions = buildHttpServerOptions();
        var vertxConsumer = new VertxHttpConsumer(getContext(), vertx, vertxOptions, httpOptions, path, method, format, getConnectorConfig().getParameters());
        this.consumer = Optional.of(vertxConsumer);
    }
}
