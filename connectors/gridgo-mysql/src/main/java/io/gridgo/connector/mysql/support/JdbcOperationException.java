package io.gridgo.connector.mysql.support;

public class JdbcOperationException extends Exception {

    public JdbcOperationException(Throwable cause) {
        super(cause);
    }

    public JdbcOperationException(String message) {
        super(message);
    }

    public JdbcOperationException() {
        super("Invalid Operation");
    }
}
