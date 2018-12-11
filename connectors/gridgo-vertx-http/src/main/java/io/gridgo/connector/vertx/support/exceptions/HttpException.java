package io.gridgo.connector.vertx.support.exceptions;

import lombok.Getter;

public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private int code;

    public HttpException(int code) {
        super(code + "");
        this.code = code;
    }
}
