package io.gridgo.connector.jdbc.support;

public class JdbcOperationException extends RuntimeException {

    public JdbcOperationException(Throwable cause) {
        super(cause);
    }

    JdbcOperationException(String message) {
        super(message);
    }

    JdbcOperationException(String message, Exception ex) {
        super(message, ex);
    }

    public JdbcOperationException() {
        super("Invalid Operation");
    }
}
