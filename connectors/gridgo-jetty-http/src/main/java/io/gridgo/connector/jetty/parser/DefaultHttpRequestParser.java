package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.jetty.support.HttpEntityHelper.parseAsMultiPart;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.httpcommon.HttpContentType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultHttpRequestParser extends AbstractHttpRequestParser {

    protected static final Set<String> NO_BODY_METHODS = new HashSet<>(Arrays.asList("get", "delete", "options"));

    private final String format;

    @Override
    protected BElement extractBody(HttpServletRequest request) throws Exception {
        if (!NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim())) {
            String reqContentType = request.getContentType();
            HttpContentType contentType = HttpContentType.forValueOrDefault(reqContentType, HttpContentType.DEFAULT_TEXT);
            if (contentType.isMultipartFormat()) {
                return parseAsMultiPart(request.getParts());
            }
            if (contentType.isJsonFormat()) {
                return BElement.ofJson(request.getInputStream());
            }
            if (contentType.isTextFormat()) {
                try (StringWriter out = new StringWriter()) {
                    request.getReader().transferTo(out);
                    return BValue.of(out.toString());
                }
            }
            if (contentType.isBinaryFormat()) {
                if (contentType == HttpContentType.APPLICATION_OCTET_STREAM) {
                    return BElement.ofBytes(request.getInputStream(), format);
                }
                return BReference.of(request.getInputStream());
            }
        }
        return null;
    }
}
