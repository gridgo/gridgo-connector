package io.gridgo.connector.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.Responder;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;

public interface JettyResponder extends Responder, FailureHandlerAware<JettyResponder> {

	public Deferred<Message, Exception> registerRequest(HttpServletRequest request);

	public void writeResponse(HttpServletResponse response, Message responseMessage);

	public Message generateFailureMessage(Throwable ex);
}
