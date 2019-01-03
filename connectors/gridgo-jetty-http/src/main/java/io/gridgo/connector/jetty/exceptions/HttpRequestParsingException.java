package io.gridgo.connector.jetty.exceptions;

public class HttpRequestParsingException extends RuntimeException {

    private static final long serialVersionUID = -3262123686698264366L;

    public HttpRequestParsingException() {
        super();
    }

    public HttpRequestParsingException(String message) {
        super(message);
    }

    public HttpRequestParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestParsingException(Throwable cause) {
        super(cause);
    }
}
