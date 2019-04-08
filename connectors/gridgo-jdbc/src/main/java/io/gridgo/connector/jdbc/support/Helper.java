package io.gridgo.connector.jdbc.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdbi.v3.core.statement.SqlStatement;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Helper {

    private Helper() {
    }

    public static List<Map<String, Object>> resultSetAsList(ResultSet resultSet) {
        var rows = new ArrayList<Map<String, Object>>();
        try {
            var md = resultSet.getMetaData();
            int columns = md.getColumnCount();
            while (resultSet.next()) {
                var row = new HashMap<String, Object>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
        } catch (SQLException sqlEx) {
            throw new JdbcOperationException("Error when converting resultSet to List", sqlEx);
        }
        return rows;
    }

    public static void bindParams(SqlStatement<?> sqlStatement, BObject params) {
        try {
            for (var entry : params.entrySet()) {
                if (entry.getValue() instanceof BValue) {
                    sqlStatement.bind(entry.getKey(), entry.getValue().asValue().getData());
                } else if (entry.getValue() instanceof BArray) {
                    sqlStatement.bindList(entry.getKey(), entry.getValue().asArray().toList());
                } else {
                    sqlStatement.bind(entry.getKey(), (Object) entry.getValue().asReference().getReference());
                }
            }
        } catch (ClassCastException ex) {
            log.error("Error while binding param to sql statement", ex);
            throw ex;
        }
    }

    public static String getOperation(String sqlStatement) {
        String trim = sqlStatement.trim();
        return trim.substring(0, trim.indexOf(' ')).toLowerCase();
    }
}
