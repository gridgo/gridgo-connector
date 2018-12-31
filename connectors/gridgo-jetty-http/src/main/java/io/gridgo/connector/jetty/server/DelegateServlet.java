package io.gridgo.connector.jetty.server;

import java.io.IOException;
import java.util.function.BiConsumer;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

class DelegateServlet extends GenericServlet {

    private static final long serialVersionUID = 2512710354394670721L;

    private final transient BiConsumer<HttpServletRequest, HttpServletResponse> handler;

    DelegateServlet(@NonNull BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
        this.handler = handler;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request;
        HttpServletResponse response;

        if (!(req instanceof HttpServletRequest && res instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP request or response");
        }

        request = (HttpServletRequest) req;
        response = (HttpServletResponse) res;

        this.handler.accept(request, response);
    }

}
