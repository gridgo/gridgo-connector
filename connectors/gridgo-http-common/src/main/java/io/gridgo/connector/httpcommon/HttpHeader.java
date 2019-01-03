package io.gridgo.connector.httpcommon;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public enum HttpHeader {

    /* ------------------------------------------------------------ */
    /**
     * General Fields.
     */
    CONNECTION("Connection", true, true), CACHE_CONTROL("Cache-Control", true, true), DATE("Date", true, true), PRAGMA("Pragma", true, true),
    PROXY_CONNECTION("Proxy-Connection", true, true), TRAILER("Trailer", true, true), TRANSFER_ENCODING("Transfer-Encoding", true, true),
    UPGRADE("Upgrade", true, true), VIA("Via", true, true), WARNING("Warning", true, true), NEGOTIATE("Negotiate", true, true),

    /* ------------------------------------------------------------ */
    /**
     * Entity Fields.
     */
    ALLOW("Allow", true, true), CONTENT_ENCODING("Content-Encoding", true, true), CONTENT_LANGUAGE("Content-Language", true, true),
    CONTENT_LENGTH("Content-Length", true, true), CONTENT_LOCATION("Content-Location", true, true), CONTENT_MD5("Content-MD5", true, true),
    CONTENT_RANGE("Content-Range", true, true), CONTENT_TYPE("Content-Type", true, true), EXPIRES("Expires", true, true),
    LAST_MODIFIED("Last-Modified", true, true),

    /* ------------------------------------------------------------ */
    /**
     * Request Fields.
     */
    ACCEPT("Accept", true, false), ACCEPT_CHARSET("Accept-Charset", true, false), ACCEPT_ENCODING("Accept-Encoding", true, false),
    ACCEPT_LANGUAGE("Accept-Language", true, false), AUTHORIZATION("Authorization", true, false), EXPECT("Expect", true, false),
    FORWARDED("Forwarded", true, false), FROM("From", true, false), HOST("Host", true, false), IF_MATCH("If-Match", true, false),
    IF_MODIFIED_SINCE("If-Modified-Since", true, false), IF_NONE_MATCH("If-None-Match", true, false), IF_RANGE("If-Range", true, false),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since", true, false), KEEP_ALIVE("Keep-Alive", true, false), MAX_FORWARDS("Max-Forwards", true, false),
    PROXY_AUTHORIZATION("Proxy-Authorization", true, false), RANGE("Range", true, false), REQUEST_RANGE("Request-Range", true, false),
    REFERER("Referer", true, false), TE("TE", true, false), USER_AGENT("User-Agent", true, false), X_FORWARDED_FOR("X-Forwarded-For", true, false),
    X_FORWARDED_PROTO("X-Forwarded-Proto", true, false), X_FORWARDED_SERVER("X-Forwarded-Server", true, false),
    X_FORWARDED_HOST("X-Forwarded-Host", true, false),

    /* ------------------------------------------------------------ */
    /**
     * Response Fields.
     */
    ACCEPT_RANGES("Accept-Ranges", false, true), AGE("Age", false, true), ETAG("ETag", false, true), LOCATION("Location", false, true),
    PROXY_AUTHENTICATE("Proxy-Authenticate", false, true), RETRY_AFTER("Retry-After", false, true), SERVER("Server", false, true),
    SERVLET_ENGINE("Servlet-Engine", false, true), VARY("Vary", false, true), WWW_AUTHENTICATE("WWW-Authenticate", false, true),

    /* ------------------------------------------------------------ */
    /**
     * WebSocket Fields.
     */
    ORIGIN("Origin", true, true), SEC_WEBSOCKET_KEY("Sec-WebSocket-Key", true, true), SEC_WEBSOCKET_VERSION("Sec-WebSocket-Version", true, true),
    SEC_WEBSOCKET_EXTENSIONS("Sec-WebSocket-Extensions", true, true), SEC_WEBSOCKET_SUBPROTOCOL("Sec-WebSocket-Protocol", true, true),
    SEC_WEBSOCKET_ACCEPT("Sec-WebSocket-Accept", true, true),

    /* ------------------------------------------------------------ */
    /**
     * Other Fields.
     */
    COOKIE("Cookie", true, true), SET_COOKIE("Set-Cookie", true, true), SET_COOKIE2("Set-Cookie2", true, true), MIME_VERSION("MIME-Version", true, true),
    IDENTITY("identity", true, true),

    X_POWERED_BY("X-Powered-By", false, true), HTTP2_SETTINGS("HTTP2-Settings", true, true),

    STRICT_TRANSPORT_SECURITY("Strict-Transport-Security", true, true),

    /* ------------------------------------------------------------ */
    /**
     * HTTP2 Fields.
     */
    C_METHOD(":method", true, true), C_SCHEME(":scheme", true, true), C_AUTHORITY(":authority", true, true), C_PATH(":path", true, true),
    C_STATUS(":status", true, true),

    UNKNOWN("::UNKNOWN::", true, true),

    /* ------------------------------------------------------------ */
    /**
     * Custom Fields
     */
    HTTP_STATUS("Http-Status", false, true, true), HTTP_METHOD("Http-Method", true, false, true), QUERY_PARAMS("Query-Params", true, false, true),
    CHARSET("Charset", true, true, true), PATH_INFO("Path-Info", true, false, true), SCHEME("Scheme", true, false, true),
    REMOTE_ADDR("Remote-Addr", true, false, true), LOCAL_ADDR("Local-Addr", true, false, true), CONTEXT_PATH("Context-Path", true, false, true);

    /* ------------------------------------------------------------ */
    private final static Map<String, HttpHeader> CACHE = new HashMap<>();
    static {
        for (HttpHeader header : HttpHeader.values()) {
            if (header != UNKNOWN) {
                CACHE.putIfAbsent(header.toString().trim().toLowerCase(), header);
            }
        }
    }

    public static final HttpHeader lookUp(String value) {
        if (value != null) {
            return CACHE.get(value.trim().toLowerCase());
        }
        return null;
    }

    private final String _string;

    @Getter
    private final boolean forRequest;

    @Getter
    private final boolean forResponse;

    @Getter
    private boolean custom = false;

    /* ------------------------------------------------------------ */
    private HttpHeader(String s, boolean forRequest, boolean forResponse) {
        _string = s;
        this.forRequest = forRequest;
        this.forResponse = forResponse;
    }

    private HttpHeader(String s, boolean forRequest, boolean forResponse, boolean custom) {
        _string = s;
        this.forRequest = forRequest;
        this.forResponse = forResponse;
        this.custom = custom;
    }

    /* ------------------------------------------------------------ */
    public String asString() {
        return _string;
    }

    /* ------------------------------------------------------------ */
    public boolean is(String s) {
        return _string.equalsIgnoreCase(s);
    }

    /* ------------------------------------------------------------ */
    @Override
    public String toString() {
        return _string;
    }
}
