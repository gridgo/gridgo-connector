package io.gridgo.connector.jdbc;

public class JdbcConstants {

    private JdbcConstants() {
    }

    public static final String OPERATION = "JDBC_Operation";

    public static final String OPERATION_SELECT = "select";

    public static final String OPERATION_UPDATE = "update";

    public static final String OPERATION_DELETE = "delete";

    public static final String OPERATION_INSERT = "insert";

    public static final String OPERATION_EXECUTE = "execute";

    static final int DEFAULT_CONNECTION_POOL_MIN_POOL_SIZE = 5;

    static final int DEFAULT_CONNECTION_POOL_MAX_POOL_SIZE = 15;

    static final int DEFAULT_CONNECTION_POOL_MAX_SIZE = 0;

    static final int DEFAULT_CONNECTION_POOL_IDLE_TIMEOUT = 180;

}
