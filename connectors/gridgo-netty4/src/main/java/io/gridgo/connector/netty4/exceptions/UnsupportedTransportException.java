package io.gridgo.connector.netty4.exceptions;

public class UnsupportedTransportException extends RuntimeException {

    private static final long serialVersionUID = 6304366906570241968L;

    public UnsupportedTransportException() {
        super();
    }

    public UnsupportedTransportException(String message) {
        super(message);
    }

    public UnsupportedTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedTransportException(Throwable cause) {
        super(cause);
    }
}
