package io.gridgo.connector.vertx;

import io.gridgo.connector.httpcommon.HttpCommonConsumerConstants;

public class VertxHttpConstants extends HttpCommonConsumerConstants {

    public static final String PLACEHOLDER_PORT = "port";

    public static final String PLACEHOLDER_HOST = "host";

    public static final String PLACEHOLDER_PATH = "path";

    public static final String PARAM_KEY_STORE_PASSWD = "keyStorePassword";

    public static final String PARAM_KEY_STORE_PATH = "keyStorePath";

    public static final String PARAM_CLIENT_AUTH = "clientAuth";

    public static final String PARAM_SSL = "ssl";

    public static final String PARAM_USE_ALPN = "useAlpn";

    public static final String PARAM_EVENT_LOOP_POOL_SIZE = "eventLoopPoolSize";

    public static final String PARAM_WORKER_POOL_SIZE = "workerPoolSize";

    public static final Object PARAM_PARSE_COOKIE = "parseCookie";

    public static final String PARAM_COMPRESSION_LEVEL = "compressionLevel";

    public static final String PARAM_VERTX_BEAN = "vertxBean";

    public static final String HEADER_COOKIE = "Parsed-Cookie";

    public static final String COOKIE_DOMAIN = "cookieDomain";

    public static final String COOKIE_PATH = "cookiePath";

    public static final String COOKIE_VALUE = "cookieValue";

    public static final String COOKIE_NAME = "cookieName";

    public static final String ACCEPT_BACKLOG = "acceptBacklog";

    public static final String PEM_CERT_PATH = "certPath";

    public static final String PEM_KEY_PATH = "keyPath";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    
    public static final String HEADER_OUTPUT_STREAM = "Gridgo-OutputStream";

    public static final Object WRAP_RESPONSE = "wrap";
}
