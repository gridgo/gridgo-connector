package io.gridgo.socket.netty4.exceptions;

public class SSLContextException extends RuntimeException {

    private static final long serialVersionUID = 1031330096086543143L;

    public SSLContextException() {
        super();
    }

    public SSLContextException(String message) {
        super(message);
    }

    public SSLContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSLContextException(Throwable cause) {
        super(cause);
    }
}
