package io.gridgo.connector.jdbc;

import org.jdbi.v3.core.Handle;

import io.gridgo.connector.jdbc.support.Helper;
import io.gridgo.framework.support.Message;

class JdbcOperator {

    // TODO: Have not supported byte[] in params yet
    static Message select(Message msg, Handle handle) {
        var queryStatement = msg.body().asValue().getString();
        var query = handle.createQuery(queryStatement);
        Helper.bindParams(query, msg.getPayload().getHeaders());
        var resultSet = query.execute((supplier, context) -> supplier.get().executeQuery());
        var rows = Helper.resultSetAsList(resultSet);
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
