package io.gridgo.connector.mysql;

import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.framework.support.Message;
import org.jdbi.v3.core.Handle;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

class JdbcOperator {

    //TODO: Have not supported byte[] in params yet
    static Message select(Message msg, Handle handle) {
        var queryStatement = msg.body().asValue().getString();
        var query = handle.createQuery(queryStatement);
        Helper.bindParams(query, msg.getPayload().getHeaders());
        ResultSet resultSet = query.execute((supplier, context) -> supplier.get().executeQuery());
        List<Map<String, Object>> rows = Helper.resultSetAsList(resultSet);
        return Message.ofAny(rows);
    }

    static Message updateRow(Message msg, Handle handle) {
        var headers = msg.getPayload().getHeaders();
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var query = handle.createUpdate(queryStatement);
        Helper.bindParams(query, headers);
        int rowNum = query.execute();
        return Message.ofAny(rowNum);
    }

    static Message execute(Message msg, Handle handle) {
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        int result = handle.execute(queryStatement);
        return Message.ofAny(result);
    }
}
