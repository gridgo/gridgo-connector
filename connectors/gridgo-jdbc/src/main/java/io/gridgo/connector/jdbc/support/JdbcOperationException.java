package io.gridgo.connector.jdbc.support;

public class JdbcOperationException extends RuntimeException {

    private static final long serialVersionUID = -7029131652408759411L;

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
