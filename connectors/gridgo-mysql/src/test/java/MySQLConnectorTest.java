import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.mysql.MySQLConstants;
import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.SimpleRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        registry = new SimpleRegistry();
        context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        connector = new DefaultConnectorFactory().createConnector("jdbc:mysql/localhost/3306/root/ManhCuong22293/test", context);
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
        var headers = BObject.ofEmpty();
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.ofAny(headers, sql);
    }

    private Message createInsertRequest() {
        String sql = buildInsertSQL();
        var headers = BObject.ofEmpty();
        columnsName.forEach(column -> headers.putAny(column, sqlValues.get(column)));
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }

    private Message createCreateTableMessage(){
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
        return Message.ofAny(queryBuilder.toString());
    }

    private Message createDropTableMessage(){
        return Message.ofAny("drop table " + tableName + " ;");
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
        addField(Date.class, "DATE", Date.valueOf(LocalDate.now()));
        addField(Time.class, "TIME", Time.valueOf(LocalTime.now()));
        addField(Timestamp.class, "TIMESTAMP", Timestamp.valueOf(LocalDateTime.now()));
    }


    @Test
    public void testSelect() {
        var ok = producer.call(createSelectRequest());
        ok.done(msg -> {
            try {
                var list = msg.getPayload().getBody().asArray();
                for (BElement bElement : list) {
                var result = bElement.asObject();
                    columnsName.forEach(column -> {
                        Assert.assertEquals(BValue.of(sqlValues.get(column)),result.get(column));
                    });
                }
            }catch (Exception ex){
                ex.printStackTrace();
                Assert.fail();
            }
        });
    }

    @Test
    public void testInsert() {
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

    @Test
    public void testDropTable(){
        Message message = createDropTableMessage();
        var ok = producer.call(message);
        ok.done(msg -> Assert.assertTrue(true))
            .fail(ex -> Assert.fail());
    }

    @Test
    public void testCreateTable(){
        Message message = createCreateTableMessage();
        var ok = producer.call(message);
        ok.done(msg -> Assert.assertTrue(true))
            .fail(ex -> Assert.fail());
    }
}
