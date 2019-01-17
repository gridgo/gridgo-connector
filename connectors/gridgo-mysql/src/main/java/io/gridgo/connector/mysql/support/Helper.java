package io.gridgo.connector.mysql.support;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.impl.MutableBValue;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.statement.SqlStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Helper {

    private Helper() {
    }

    public static List<Map<String, Object>> resultSetAsList(ResultSet resultSet) {
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
        } catch (SQLException sqlEx) {
            throw new JdbcOperationException("Error when converting resultSet to List", sqlEx);
        }
        return rows;
    }

    public static void bindParams(SqlStatement<?> sqlStatement, BObject params) {
        try {
            for (Map.Entry<String, BElement> entry : params.entrySet()) {
                if (entry.getValue() instanceof MutableBValue) {
                    sqlStatement.bind(entry.getKey(), entry.getValue().asValue().getData());
                } else {
                    sqlStatement.bind(entry.getKey(), (Object) entry.getValue().asReference().getReference());
                }
            }
        } catch (ClassCastException ex) {
            log.error("Error while binding param to sql statement", ex);
            throw ex;
        }
    }
}
