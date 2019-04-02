package io.gridgo.connector.jdbc;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtil {
    private List<String> columnsName;
    private Map<String, String> sqlTypes;
    private Map<String, Object> sqlValues;
    private String tableName;

    TestUtil(String tableName) {
        this.tableName = tableName;
        columnsName = new ArrayList<>();
        sqlTypes = new HashMap<>();
        sqlValues = new HashMap<>();
        createDefaultFieldList();
    }

    private String buildInsertSQL() {
        StringBuilder sql = new StringBuilder("insert into ").append(tableName).append(" ( ");
        columnsName.forEach(column -> {
            sql.append(column);
            sql.append(", ");
        });
        sql.replace(sql.length() - 2, sql.length() - 1, ")");
        sql.append(" values ( ");
        columnsName.forEach(column -> {
            sql.append(":");
            sql.append(column);
            sql.append(", ");
        });
        sql.deleteCharAt(sql.length() - 2);
        sql.append(" );");
        return sql.toString();
    }

    private String buildSelectSQL() {
        StringBuilder sql = new StringBuilder("select * from ").append(tableName).append(" where ");
        columnsName.forEach(column -> {
            sql.append(column);
            sql.append(" = ");
            sql.append(":");
            sql.append(column);
            sql.append(" AND ");
        });
        sql.replace(sql.length() - 4, sql.length(), "   ;");
        return sql.toString();
    }

    Message createSelectRequest() {
        String sql = buildSelectSQL();
        var headers = BObject.ofEmpty();
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.ofAny(headers, sql);
    }

    Message createInsertRequest() {
        String sql = buildInsertSQL();
        var headers = BObject.ofEmpty();
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }

    Message createCreateTableMessage() {
        var headers = BObject.ofEmpty();
        StringBuilder queryBuilder = new StringBuilder("create table ").append(tableName).append(" ( ");
        for (String column : columnsName) {
            queryBuilder.append(column);
            queryBuilder.append(" ");
            queryBuilder.append(sqlTypes.get(column));
            queryBuilder.append(",");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(");");

        return Message.ofAny(headers, queryBuilder.toString());
    }

    Message createDropTableMessage() {
        var headers = BObject.ofEmpty();
        return Message.ofAny(headers, "drop table if exists " + tableName + " ;");
    }

    void addField(Field field) {
        columnsName.add(field.fieldName);
        sqlTypes.put(field.fieldName, field.sqlType);
        sqlValues.put(field.fieldName, field.value);
    }

    void createDefaultFieldList() {
        addField(new Field(Integer.class, "INTEGER", 112));
        addField(new Field(String.class, "VARCHAR(200)", "test"));
        addField(new Field(BigDecimal.class, "DECIMAL", new BigDecimal(21323)));
        addField(new Field(Boolean.class, "BIT", true));
//        addField(new Field("arraybyte", "BINARY(100)", "t".getBytes()));
        addField(new Field(Date.class, "DATE", Date.valueOf(LocalDate.parse("2019-01-10"))));
        addField(new Field(Time.class, "TIME", Time.valueOf(LocalTime.parse("19:49:06"))));
        addField(
                new Field(Timestamp.class, "TIMESTAMP", Timestamp.valueOf(LocalDateTime.parse("2007-12-03T10:15:30"))));
    }

    public List<String> getColumnsName() {
        return columnsName;
    }

    public void setColumnsName(List<String> columnsName) {
        this.columnsName = columnsName;
    }

    public Map<String, Object> getSqlValues() {
        return sqlValues;
    }

    public void setSqlValues(Map<String, Object> sqlValues) {
        this.sqlValues = sqlValues;
    }
}
