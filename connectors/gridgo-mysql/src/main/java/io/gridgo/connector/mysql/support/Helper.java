package io.gridgo.connector.mysql.support;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {

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
            return null;
        }
        return rows;
    }
}
