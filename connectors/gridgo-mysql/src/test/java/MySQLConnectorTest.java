import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorFactory;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.mysql.MySQLConstants;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.SimpleRegistry;
import org.jdbi.v3.core.ConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import snaq.db.ConnectionPool;

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

public class MySQLConnectorTest {

    private Registry registry;
    private ConnectorContext context;
    private Connector connector;
    private Producer producer;
    private List<String> columnsName;
    private Map<String, String> sqlTypes;
    private Map<String, Object> sqlValues;
    private String tableName;

    @Before
    public void initialize(){
        var pool = new ConnectionPool("local", 5, 15, 0, 180, "jdbc:mysql://localhost:3306/test", "root", "1");
        registry = new SimpleRegistry().register("sonaq", (ConnectionFactory)pool::getConnection);
        context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        connector = new DefaultConnectorFactory().createConnector("jdbc:mysql://localhost:3306/test?user=root&password=1&pool=sonaq", context);
        connector.start();
        producer = connector.getProducer().orElseThrow();
        tableName = "table_test_mysql_connector";
        columnsName = new ArrayList<>();
        sqlTypes = new HashMap<>();
        sqlValues = new HashMap<>();
        createFieldList();
    }



    private String buildInsertSQL(){
        StringBuilder sql = new StringBuilder("insert into ")
                .append(tableName)
                .append(" ( ");
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

    private String buildSelectSQL(){
        StringBuilder sql = new StringBuilder("select * from ")
                .append(tableName)
                .append(" where ");
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

    private Message createSelectRequest() {
        String sql = buildSelectSQL();
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_SELECT);
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.ofAny(headers, sql);
    }

    private Message createInsertRequest() {
        String sql = buildInsertSQL();
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_INSERT);
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }

    private Message createCreateTableMessage(){
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION,MySQLConstants.OPERATION_EXCUTE);
        StringBuilder queryBuilder = new StringBuilder("create table ")
                .append(tableName)
                .append(" ( ");
        for (String column : columnsName){
            queryBuilder.append(column);
            queryBuilder.append(" ");
            queryBuilder.append(sqlTypes.get(column));
            queryBuilder.append(",");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(");");

        return Message.ofAny(headers, queryBuilder.toString());
    }

    private Message createDropTableMessage(){
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_EXCUTE);;
        return Message.ofAny(headers, "drop table if exists " + tableName + " ;");
    }

    private void addField(Class type,String sqlType, Object value){
        String columnName = type.getSimpleName().toLowerCase() + "test";
        columnsName.add(columnName);
        sqlTypes.put(columnName, sqlType);
        sqlValues.put(columnName, value);
    }

    private void addField(String type, String sqlType, Object value){
        String columnName = type + "test";
        columnsName.add(columnName);
        sqlTypes.put(columnName, sqlType);
        sqlValues.put(columnName, value);
    }

    private void createFieldList(){
        addField(Integer.class, "INTEGER", 1);
        addField(String.class, "VARCHAR(200)", "test");
        addField(BigDecimal.class, "DECIMAL", new BigDecimal(21323));
        addField(Boolean.class, "BIT", true);
//        addField("arraybyte", "BINARY(100)", "t".getBytes());
        addField(Date.class, "DATE", Date.valueOf(LocalDate.parse("2019-01-10")));
        addField(Time.class, "TIME", Time.valueOf(LocalTime.parse("19:49:06")));
        addField(Timestamp.class, "TIMESTAMP", Timestamp.valueOf(LocalDateTime.parse("2007-12-03T10:15:30")));
    }



    @Test
    public void testSelect() {
        dropTable();
        testCreateTable();
        testInsert();
        var ok = producer.call(createSelectRequest());
        ok.done(msg -> {
            try {
                var list = msg.getPayload().getBody().asArray();
                for (BElement bElement : list) {
                    var result = bElement.asObject();
                    Assert.assertEquals(sqlValues.get("integertest"), result.getInteger("integertest"));
                    Assert.assertEquals(sqlValues.get("stringtest"), result.getString("stringtest"));
                    Assert.assertEquals(sqlValues.get("bigdecimaltest"), result.get("bigdecimaltest").asValue().getDataAs(BigDecimal.class));
                    Assert.assertEquals(sqlValues.get("booleantest"), result.getBoolean("booleantest"));
                    Assert.assertEquals(sqlValues.get("datetest"), result.get("datetest").asReference().getReference());
                    Assert.assertEquals(sqlValues.get("timetest"), result.get("timetest").asReference().getReference());
                    Assert.assertEquals(sqlValues.get("timestamptest"), result.get("timestamptest").asReference().getReference());
                }
            }catch (Exception ex){
                ex.printStackTrace();
                Assert.fail();
            }
        });
    }


     private void testInsert() {
        var ok = producer.call(createInsertRequest());
        ok.done(msg -> {
            var list = msg.getPayload().getBody().asValue().getInteger();
            Assert.assertEquals(Integer.valueOf(1), list);
        });
        ok.fail(ex -> {
            ex.printStackTrace();
            Assert.fail();
        });
    }


    private void dropTable(){
        Message message = createDropTableMessage();
        producer.call(message);
    }



    private void testCreateTable(){
        dropTable();
        Message message = createCreateTableMessage();
        var ok = producer.call(message);
        ok.done(msg -> Assert.assertTrue(true))
            .fail(ex -> {
                ex.printStackTrace();
                Assert.fail();
            });
    }
}
