package io.gridgo.connector.mysql.support;

public class MySQLOperationException extends Exception {


    public MySQLOperationException(Throwable cause) {
        super(cause);
    }

    public MySQLOperationException(String message) {
        super(message);
    }

    public MySQLOperationException() {
        super("Invalid Operation");
    }
}
