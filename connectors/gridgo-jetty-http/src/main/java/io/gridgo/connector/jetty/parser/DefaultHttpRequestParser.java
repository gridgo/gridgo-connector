package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.httpcommon.HttpContentType.APPLICATION_OCTET_STREAM;
import static io.gridgo.connector.httpcommon.HttpContentType.DEFAULT_TEXT;
import static io.gridgo.connector.jetty.support.HttpEntityHelper.parseAsMultiPart;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.httpcommon.HttpContentType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultHttpRequestParser extends AbstractHttpRequestParser {

    private final String format;

    @Override
    protected BElement extractBody(HttpServletRequest request) throws Exception {
        if (!NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim())) {

            HttpContentType contentType = HttpContentType.forValueOrDefault(request.getContentType(), DEFAULT_TEXT);

            if (contentType == APPLICATION_OCTET_STREAM) {
                return BElement.ofBytes(request.getInputStream(), format);
            }

            if (contentType.isMultipartFormat()) {
                return parseAsMultiPart(request.getParts());
            }

            if (contentType.isJsonFormat()) {
                return BElement.ofJson(request.getInputStream());
            }

            if (contentType.isTextFormat()) {
                StringWriter out = new StringWriter();
                request.getReader().transferTo(out);
                return BValue.of(out.toString());
            }

            if (contentType.isBinaryFormat()) {
                return BReference.of(request.getInputStream());
            }
        }
        return null;
    }
}
