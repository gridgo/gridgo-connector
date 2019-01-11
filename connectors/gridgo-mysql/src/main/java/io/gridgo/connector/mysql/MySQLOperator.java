package io.gridgo.connector.mysql;

import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.framework.support.Message;
import org.jdbi.v3.core.Handle;
import org.joo.promise4j.Deferred;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

class MySQLOperator {

    //TODO: Have not supported byte[] in params yet
    static void select(Message msg, Handle handle, Deferred<Message, Exception> deferred){
        var queryStatement = msg.body().asValue().getString();
        var query = handle.createQuery(queryStatement);
        Helper.bindParams(query, msg.getPayload().getHeaders());
        ResultSet resultSet =  query.execute((supplier, context) -> supplier.get().executeQuery());
        List<Map<String, Object>> rows = Helper.resultSetAsList(resultSet);
        Helper.ack(deferred, rows, null);
    }

    static void updateRow(Message msg, Handle handle, Deferred<Message, Exception> deferred)  {
        var headers = msg.getPayload().getHeaders();
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var query = handle.createUpdate(queryStatement);
        Helper.bindParams(query, headers);
        int rowNum =  query.execute();
        Helper.ack(deferred, rowNum, null);
    }

    static void execute(Message msg, Handle handle, Deferred<Message, Exception> deferred){
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        int result = handle.execute(queryStatement);
        Helper.ack(deferred, result, null);
    }

    static void begin(Message msg, Handle handle, Deferred<Message, Exception> deferred){
        handle.begin();
        Transaction transaction = new Transaction(handle);
        Helper.ack(deferred, transaction, null);
    }
}
