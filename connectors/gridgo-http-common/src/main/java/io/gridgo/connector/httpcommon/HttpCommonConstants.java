package io.gridgo.connector.httpcommon;

public class HttpCommonConstants {

    public static final int DEFAULT_PROXY_PORT = 80;

    public static final String PARAM_FORMAT = "format";

    public static final String PARAM_METHOD = "method";

    public static final String PARAM_COMPRESSION_SUPPORTED = "gzip";

    public static final String COOKIES = "cookies";
    public static final String LOCALE = "locale";
    public static final String LOCAL_NAME = "localName";
    public static final String SERVER_NAME = "serverName";
    public static final String SERVER_PORT = "serverPort";
    public static final String LOCALES = "locales";
    public static final String SESSION = "session";
    public static final String USER_PRINCIPAL = "userPrincipal";

    public static final String QUERY = "query";
    public static final String BODY = "body";
    public static final String NAME = "name";
    public static final String SUBMITTED_FILE_NAME = "fileName";
    public static final String HEADERS = "headers";
    public static final String UTF_8 = "UTF-8";

    public static final String CONNECTION = HttpHeader.CONNECTION.asString();
    public static final String CACHE_CONTROL = HttpHeader.CACHE_CONTROL.asString();
    public static final String DATE = HttpHeader.DATE.asString();
    public static final String PRAGMA = HttpHeader.PRAGMA.asString();
    public static final String PROXY_CONNECTION = HttpHeader.PROXY_CONNECTION.asString();
    public static final String TRAILER = HttpHeader.TRAILER.asString();
    public static final String TRANSFER_ENCODING = HttpHeader.TRANSFER_ENCODING.asString();
    public static final String UPGRADE = HttpHeader.UPGRADE.asString();
    public static final String VIA = HttpHeader.VIA.asString();
    public static final String WARNING = HttpHeader.WARNING.asString();
    public static final String NEGOTIATE = HttpHeader.NEGOTIATE.asString();
    public static final String ALLOW = HttpHeader.ALLOW.asString();
    public static final String CONTENT_ENCODING = HttpHeader.CONTENT_ENCODING.asString();
    public static final String CONTENT_LANGUAGE = HttpHeader.CONTENT_LANGUAGE.asString();
    public static final String CONTENT_LENGTH = HttpHeader.CONTENT_LENGTH.asString();
    public static final String CONTENT_LOCATION = HttpHeader.CONTENT_LOCATION.asString();
    public static final String CONTENT_MD5 = HttpHeader.CONTENT_MD5.asString();
    public static final String CONTENT_RANGE = HttpHeader.CONTENT_RANGE.asString();
    public static final String CONTENT_TYPE = HttpHeader.CONTENT_TYPE.asString();
    public static final String EXPIRES = HttpHeader.EXPIRES.asString();
    public static final String LAST_MODIFIED = HttpHeader.LAST_MODIFIED.asString();
    public static final String ACCEPT = HttpHeader.ACCEPT.asString();
    public static final String ACCEPT_CHARSET = HttpHeader.ACCEPT_CHARSET.asString();
    public static final String ACCEPT_ENCODING = HttpHeader.ACCEPT_ENCODING.asString();
    public static final String ACCEPT_LANGUAGE = HttpHeader.ACCEPT_LANGUAGE.asString();
    public static final String AUTHORIZATION = HttpHeader.AUTHORIZATION.asString();
    public static final String EXPECT = HttpHeader.EXPECT.asString();
    public static final String FORWARDED = HttpHeader.FORWARDED.asString();
    public static final String FROM = HttpHeader.FROM.asString();
    public static final String HOST = HttpHeader.HOST.asString();
    public static final String IF_MATCH = HttpHeader.IF_MATCH.asString();
    public static final String IF_MODIFIED_SINCE = HttpHeader.IF_MODIFIED_SINCE.asString();
    public static final String IF_NONE_MATCH = HttpHeader.IF_NONE_MATCH.asString();
    public static final String IF_RANGE = HttpHeader.IF_RANGE.asString();
    public static final String IF_UNMODIFIED_SINCE = HttpHeader.IF_UNMODIFIED_SINCE.asString();
    public static final String KEEP_ALIVE = HttpHeader.KEEP_ALIVE.asString();
    public static final String MAX_FORWARDS = HttpHeader.MAX_FORWARDS.asString();
    public static final String PROXY_AUTHORIZATION = HttpHeader.PROXY_AUTHORIZATION.asString();
    public static final String RANGE = HttpHeader.RANGE.asString();
    public static final String REQUEST_RANGE = HttpHeader.REQUEST_RANGE.asString();
    public static final String REFERER = HttpHeader.REFERER.asString();
    public static final String TE = HttpHeader.TE.asString();
    public static final String USER_AGENT = HttpHeader.USER_AGENT.asString();
    public static final String X_FORWARDED_FOR = HttpHeader.X_FORWARDED_FOR.asString();
    public static final String X_FORWARDED_PROTO = HttpHeader.X_FORWARDED_PROTO.asString();
    public static final String X_FORWARDED_SERVER = HttpHeader.X_FORWARDED_SERVER.asString();
    public static final String X_FORWARDED_HOST = HttpHeader.X_FORWARDED_HOST.asString();
    public static final String ACCEPT_RANGES = HttpHeader.ACCEPT_RANGES.asString();
    public static final String AGE = HttpHeader.AGE.asString();
    public static final String ETAG = HttpHeader.ETAG.asString();
    public static final String LOCATION = HttpHeader.LOCATION.asString();
    public static final String PROXY_AUTHENTICATE = HttpHeader.PROXY_AUTHENTICATE.asString();
    public static final String RETRY_AFTER = HttpHeader.RETRY_AFTER.asString();
    public static final String SERVER = HttpHeader.SERVER.asString();
    public static final String SERVLET_ENGINE = HttpHeader.SERVLET_ENGINE.asString();
    public static final String VARY = HttpHeader.VARY.asString();
    public static final String WWW_AUTHENTICATE = HttpHeader.WWW_AUTHENTICATE.asString();
    public static final String ORIGIN = HttpHeader.ORIGIN.asString();
    public static final String SEC_WEBSOCKET_KEY = HttpHeader.SEC_WEBSOCKET_KEY.asString();
    public static final String SEC_WEBSOCKET_VERSION = HttpHeader.SEC_WEBSOCKET_VERSION.asString();
    public static final String SEC_WEBSOCKET_EXTENSIONS = HttpHeader.SEC_WEBSOCKET_EXTENSIONS.asString();
    public static final String SEC_WEBSOCKET_SUBPROTOCOL = HttpHeader.SEC_WEBSOCKET_SUBPROTOCOL.asString();
    public static final String SEC_WEBSOCKET_ACCEPT = HttpHeader.SEC_WEBSOCKET_ACCEPT.asString();
    public static final String COOKIE = HttpHeader.COOKIE.asString();
    public static final String SET_COOKIE = HttpHeader.SET_COOKIE.asString();
    public static final String SET_COOKIE2 = HttpHeader.SET_COOKIE2.asString();
    public static final String MIME_VERSION = HttpHeader.MIME_VERSION.asString();
    public static final String IDENTITY = HttpHeader.IDENTITY.asString();
    public static final String X_POWERED_BY = HttpHeader.X_POWERED_BY.asString();
    public static final String HTTP2_SETTINGS = HttpHeader.HTTP2_SETTINGS.asString();
    public static final String STRICT_TRANSPORT_SECURITY = HttpHeader.STRICT_TRANSPORT_SECURITY.asString();
    public static final String C_METHOD = HttpHeader.C_METHOD.asString();
    public static final String C_SCHEME = HttpHeader.C_SCHEME.asString();
    public static final String C_AUTHORITY = HttpHeader.C_AUTHORITY.asString();
    public static final String C_PATH = HttpHeader.C_PATH.asString();
    public static final String C_STATUS = HttpHeader.C_STATUS.asString();
    public static final String UNKNOWN = HttpHeader.UNKNOWN.asString();

    public static final String HEADER_STATUS = HttpHeader.HTTP_STATUS.asString();
    public static final String HEADER_HTTP_METHOD = HttpHeader.HTTP_METHOD.asString();
    public static final String HEADER_QUERY_PARAMS = HttpHeader.QUERY_PARAMS.asString();
    public static final String CHARSET = HttpHeader.CHARSET.asString();
    public static final String PATH_INFO = HttpHeader.PATH_INFO.asString();
    public static final String SCHEME = HttpHeader.SCHEME.asString();
    public static final String REMOTE_ADDR = HttpHeader.REMOTE_ADDR.asString();
    public static final String LOCAL_ADDR = HttpHeader.LOCAL_ADDR.asString();
    public static final String CONTEXT_PATH = HttpHeader.CONTEXT_PATH.asString();

    public static final String HEADER_STATUS_CODE = "Http-Status-Code";
    
    public static final String HEADER_HTTP_HEADERS = "Http-Headers";

    public static final String HEADER_PATH = "Http-Path";
}
