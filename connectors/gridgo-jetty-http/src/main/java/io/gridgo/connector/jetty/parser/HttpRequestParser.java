package io.gridgo.connector.jetty.parser;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.framework.support.Message;
import io.gridgo.jetty.JettyServletContextHandlerOption;

public interface HttpRequestParser {

	public static final HttpRequestParser DEFAULT = new JsonBodyHttpRequestParser();

	public Message parse(HttpServletRequest request, Set<JettyServletContextHandlerOption> options);
}
