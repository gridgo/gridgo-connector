package io.gridgo.connector.jdbc;

public class Field {
    String fieldName;
    String sqlType;
    Object value;

    Field(Class<?> type, String sqlType, Object value) {
        this.fieldName = type.getSimpleName().toLowerCase() + "test";
        this.sqlType = sqlType;
        this.value = value;
    }

    public Field(String fieldName, String sqlType, Object value) {
        this.fieldName = fieldName + "test";
        this.sqlType = sqlType;
        this.value = value;
    }
}
