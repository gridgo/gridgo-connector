package io.gridgo.socket.nanomsg;

public class NNException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NNException() {
        super();
    }

    public NNException(String message) {
        super(message);
    }

    public NNException(String message, Throwable cause) {
        super(message, cause);
    }

    public NNException(Throwable cause) {
        super(cause);
    }
}
