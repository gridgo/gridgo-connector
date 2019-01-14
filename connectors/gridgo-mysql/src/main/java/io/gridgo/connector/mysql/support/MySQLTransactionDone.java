package io.gridgo.connector.mysql.support;

public class MySQLTransactionDone extends Exception {



    public MySQLTransactionDone(Throwable cause) {
        super(cause);
    }

    public MySQLTransactionDone(){
        super("Transaction already done!!!");
    }
}
