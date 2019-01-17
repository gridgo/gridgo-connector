package io.gridgo.connector.mysql;

public class JdbcConstants {

    private JdbcConstants() {
    }

    public static final String OPERATION = "JDBC_Operation";

    public static final String OPERATION_SELECT = "JDBC_select";

    public static final String OPERATION_UPDATE = "JDBC_update";

    public static final String OPERATION_DELETE = "JDBC_delete";

    public static final String OPERATION_INSERT = "JDBC_insert";

    public static final String OPERATION_EXCUTE = "JDBC_excute";

    static final int DEFAULT_CONNECTION_POOL_MIN_POOL_SIZE = 5;

    static final int DEFAULT_CONNECTION_POOL_MAX_POOL_SIZE = 15;

    static final int DEFAULT_CONNECTION_POOL_MAX_SIZE = 0;

    static final int DEFAULT_CONNECTION_POOL_IDLE_TIMEOUT = 180;

}
