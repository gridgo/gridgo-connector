package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.jetty.support.HttpEntityHelper.parseAsMultiPart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.httpcommon.HttpContentType;
import io.gridgo.connector.httpcommon.HttpHeader;
import io.gridgo.connector.jetty.exceptions.HttpRequestParsingException;
import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.helper.Loggable;
import lombok.NonNull;

public abstract class AbstractHttpRequestParser implements HttpRequestParser, Loggable {

    protected static final Set<String> NO_BODY_METHODS = new HashSet<>(Arrays.asList("get", "delete", "options"));

    @Override
    public Message parse(@NonNull HttpServletRequest request, Set<JettyServletContextHandlerOption> options) {
        BObject headers = extractHeaders(request);
        BElement body;
        try {
            body = extractBody(request);
            var message = Message.of(Payload.of(headers, body)) //
                                 .addMisc(HttpCommonConstants.COOKIES, request.getCookies()) //

                                 .addMisc(HttpCommonConstants.LOCAL_NAME, request.getLocalName()) //
                                 .addMisc(HttpCommonConstants.SERVER_NAME, request.getServerName()) //
                                 .addMisc(HttpCommonConstants.SERVER_PORT, request.getServerPort()) //

                                 .addMisc(HttpCommonConstants.LOCALE, request.getLocale()) //
                                 .addMisc(HttpCommonConstants.LOCALES, request.getLocales()) //

                                 .addMisc(HttpCommonConstants.USER_PRINCIPAL, request.getUserPrincipal()) //
            ;
            if (options != null && options.contains(JettyServletContextHandlerOption.SESSIONS)) {
                message.addMisc(HttpCommonConstants.SESSION, request.getSession());
            }
            return message;
        } catch (Exception e) {
            throw new HttpRequestParsingException("Error while parsing http servlet request", e);
        }

    }

    protected BObject extractHeaders(HttpServletRequest request) {
        BObject result = BObject.ofEmpty();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            result.putAny(headerName, request.getHeader(headerName));
        }

        String queryString = request.getQueryString();
        String encoding = request.getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            encoding = HttpCommonConstants.UTF_8;
        }

        result.putAny(HttpHeader.CHARSET.asString(), encoding);
        result.putAny(HttpHeader.CONTENT_LENGTH.asString(), request.getContentLength());

        // custom extract query string to prevent the request auto parse multipart data
        result.putAny(HttpHeader.QUERY_PARAMS.asString(),
                BObject.of(extractQueryString(queryString, Charset.forName(encoding))));

        result.putAny(HttpHeader.SCHEME.asString(), request.getScheme());
        result.putAny(HttpHeader.HTTP_METHOD.asString(), request.getMethod());

        result.putAny(HttpHeader.CONTEXT_PATH.asString(), request.getContextPath());
        result.putAny(HttpHeader.PATH_INFO.asString(), request.getPathInfo());

        result.putAny(HttpHeader.LOCAL_ADDR.asString(), request.getLocalAddr());
        result.putAny(HttpHeader.REMOTE_ADDR.asString(), request.getRemoteAddr());

        return result;
    }

    protected Map<String, String> extractQueryString(String query, Charset charset) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        if (query != null && !query.isBlank()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                if (pair.isBlank()) {
                    continue;
                }

                int idx = pair.indexOf('=');
                if (idx < 0) {
                    queryPairs.put(pair, "");
                } else if (idx == 0) {
                    queryPairs.put("", URLDecoder.decode(pair.substring(idx + 1), charset));
                } else {
                    queryPairs.put(URLDecoder.decode(pair.substring(0, idx), charset),
                            URLDecoder.decode(pair.substring(idx + 1), charset));
                }
            }
        }
        return queryPairs;
    }

    protected BElement extractBody(HttpServletRequest request) throws Exception {
        if (!NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim())) {
            String contentType = request.getContentType();
            if (contentType != null
                    && contentType.trim().toLowerCase().contains(HttpContentType.MULTIPART_FORM_DATA.getMime())) {
                return extractMultiPartBody(request.getParts());
            } else {
                try (InputStream is = request.getInputStream()) {
                    return extractInputStreamBody(is);
                } catch (IOException e) {
                    throw new HttpRequestParsingException("Error while reading request's body as input stream", e);
                }
            }
        }
        return null;
    }

    protected abstract BElement extractInputStreamBody(InputStream inputStream);

    protected BArray extractMultiPartBody(Collection<Part> parts) throws Exception {
        return parseAsMultiPart(parts);
    }
}
