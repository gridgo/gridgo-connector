package io.gridgo.connector.mysql.support;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.impl.MutableBValue;
import io.gridgo.framework.support.Message;
import org.jdbi.v3.core.statement.SqlStatement;
import org.joo.promise4j.Deferred;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {

    private Helper(){}

    public static List<Map<String, Object>> resultSetAsList(ResultSet resultSet){
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            return new ArrayList<>();
        }
        return rows;
    }

    public static void  bindParams(SqlStatement sqlStatement, BObject params){
        try {
            for (Map.Entry<String, BElement> entry: params.entrySet()) {
                if (entry.getValue() instanceof MutableBValue) {
                    sqlStatement.bind(entry.getKey(), entry.getValue().asValue().getData());
                } else {
                    sqlStatement.bind(entry.getKey(), (Object) entry.getValue().asReference().getReference());
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public static String getOperation(String sqlStatement){
        for (int i = 0; i < sqlStatement.length(); i++) {
            if (sqlStatement.charAt(i) == ' ') {
                return sqlStatement.substring(0, i);
            }
        }
        return "";
    }

    public static void ack(Deferred<Message, Exception> deferred, Object result, Throwable throwable) {
        if (deferred == null) {
            return;
        }
        if (throwable != null) {
            deferred.reject((Exception) throwable);
            return;
        }
        deferred.resolve(Message.ofAny(result));
    }



}
