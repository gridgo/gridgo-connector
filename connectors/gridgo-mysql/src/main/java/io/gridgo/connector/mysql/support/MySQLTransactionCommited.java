package io.gridgo.connector.mysql.support;

public class MySQLTransactionCommited extends Exception {



    public MySQLTransactionCommited(Throwable cause) {
        super(cause);
    }

    public MySQLTransactionCommited(){
        super("Transaction already commited!!!");
    }
}
