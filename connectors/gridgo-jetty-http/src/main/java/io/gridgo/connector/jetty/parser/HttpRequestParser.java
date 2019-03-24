package io.gridgo.connector.jetty.parser;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.framework.support.Message;

public interface HttpRequestParser {

    static HttpRequestParser newDefault(String format) {
        return new DefaultHttpRequestParser(format);
    }

    Message parse(HttpServletRequest request, Set<JettyServletContextHandlerOption> options);
}
