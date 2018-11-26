package io.gridgo.jetty.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import lombok.NonNull;

public class HttpHeaders {

	/*
	 * custom headers
	 */
	public static final String STATUS_CODE = "___status_code___";

	/* ------------------------------------------------------------ */
	/**
	 * General Fields.
	 */
	public static String CONNECTION = "Connection";
	public static String CACHE_CONTROL = "Cache-Control";
	public static String DATE = "Date";
	public static String PRAGMA = "Pragma";

	public static String PROXY_CONNECTION = "Proxy-Connection";

	public static String TRAILER = "Trailer";
	public static String TRANSFER_ENCODING = "Transfer-Encoding";
	public static String UPGRADE = "Upgrade";
	public static String VIA = "Via";
	public static String WARNING = "Warning";
	public static String NEGOTIATE = "Negotiate";

	/* ------------------------------------------------------------ */
	/**
	 * Entity Fields.
	 */
	public static String ALLOW = "Allow";
	public static String CONTENT_ENCODING = "Content-Encoding";
	public static String CONTENT_LANGUAGE = "Content-Language";
	public static String CONTENT_LENGTH = "Content-Length";
	public static String CONTENT_LOCATION = "Content-Location";
	public static String CONTENT_MD5 = "Content-MD5";
	public static String CONTENT_RANGE = "Content-Range";
	public static String CONTENT_TYPE = "Content-Type";
	public static String EXPIRES = "Expires";
	public static String LAST_MODIFIED = "Last-Modified";

	/* ------------------------------------------------------------ */
	/**
	 * Request Fields.
	 */
	public static String ACCEPT = "Accept";
	public static String ACCEPT_CHARSET = "Accept-Charset";
	public static String ACCEPT_ENCODING = "Accept-Encoding";
	public static String ACCEPT_LANGUAGE = "Accept-Language";
	public static String AUTHORIZATION = "Authorization";
	public static String EXPECT = "Expect";
	public static String FORWARDED = "Forwarded";
	public static String FROM = "From";
	public static String HOST = "Host";
	public static String IF_MATCH = "If-Match";
	public static String IF_MODIFIED_SINCE = "If-Modified-Since";
	public static String IF_NONE_MATCH = "If-None-Match";
	public static String IF_RANGE = "If-Range";
	public static String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
	public static String KEEP_ALIVE = "Keep-Alive";
	public static String MAX_FORWARDS = "Max-Forwards";
	public static String PROXY_AUTHORIZATION = "Proxy-Authorization";
	public static String RANGE = "Range";
	public static String REQUEST_RANGE = "Request-Range";
	public static String REFERER = "Referer";
	public static String TE = "TE";
	public static String USER_AGENT = "User-Agent";
	public static String X_FORWARDED_FOR = "X-Forwarded-For";
	public static String X_FORWARDED_PROTO = "X-Forwarded-Proto";
	public static String X_FORWARDED_SERVER = "X-Forwarded-Server";
	public static String X_FORWARDED_HOST = "X-Forwarded-Host";

	/* ------------------------------------------------------------ */
	/**
	 * Response Fields.
	 */
	public static String ACCEPT_RANGES = "Accept-Ranges";
	public static String AGE = "Age";
	public static String ETAG = "ETag";
	public static String LOCATION = "Location";
	public static String PROXY_AUTHENTICATE = "Proxy-Authenticate";
	public static String RETRY_AFTER = "Retry-After";
	public static String SERVER = "Server";
	public static String SERVLET_ENGINE = "Servlet-Engine";
	public static String VARY = "Vary";
	public static String WWW_AUTHENTICATE = "WWW-Authenticate";

	/* ------------------------------------------------------------ */
	/**
	 * WebSocket Fields.
	 */
	public static String ORIGIN = "Origin";
	public static String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
	public static String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
	public static String SEC_WEBSOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
	public static String SEC_WEBSOCKET_SUBPROTOCOL = "Sec-WebSocket-Protocol";
	public static String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

	/* ------------------------------------------------------------ */
	/**
	 * Other Fields.
	 */
	public static String COOKIE = "Cookie";
	public static String SET_COOKIE = "Set-Cookie";
	public static String SET_COOKIE2 = "Set-Cookie2";
	public static String MIME_VERSION = "MIME-Version";
	public static String IDENTITY = "identity";
	public static String X_POWERED_BY = "X-Powered-By";
	public static String HTTP2_SETTINGS = "HTTP2-Settings";
	public static String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

	/* ------------------------------------------------------------ */
	/**
	 * HTTP2 Fields.
	 */
	public static String C_METHOD = ":method";
	public static String C_SCHEME = ":scheme";
	public static String C_AUTHORITY = ":authority";
	public static String C_PATH = ":path";
	public static String C_STATUS = ":status";
	public static String UNKNOWN = "::UNKNOWN::";

	private static final Map<String, String> VALID_HEADERS = new HashMap<>();
	static {
		for (HttpHeader header : HttpHeader.values()) {
			String headerName = header.asString().trim();
			VALID_HEADERS.put(headerName.toLowerCase(), headerName);
		}
	}

	public static String convertToStandardHeaderName(@NonNull String headerName) {
		return VALID_HEADERS.get(headerName.toLowerCase());
	}

	public static void writeHeaders(@NonNull BObject headers, @NonNull HttpServletResponse response) {
		for (Entry<String, BElement> entry : headers.entrySet()) {
			if (entry.getValue().isValue() && !entry.getValue().asValue().isNull()) {
				String stdHeaderName = convertToStandardHeaderName(entry.getKey());
				if (stdHeaderName != null) {
					response.addHeader(stdHeaderName, entry.getValue().asValue().getString());
				} else if (STATUS_CODE.equalsIgnoreCase(entry.getKey())) {
					response.setStatus(entry.getValue().asValue().getInteger());
				}
			}
		}
	}
}
