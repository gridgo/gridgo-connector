import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.mysql.MySQLConstants;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.SimpleRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLConnectorTest {

    Registry registry;
    ConnectorContext context;
    Connector connector;
    Producer producer;
    private Message createSelectRequest() {
        String sql = "select name, age from test_table where name=:name and age=:age";
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_SELECT)
                .setAny("name", "Cuong")
                .setAny("age", 20);
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }

    private Message createInsertRequest() {
        String sql = "insert into test1 (id, name, gender, birthday, start, exist) values" +
                "(id=:id, name=:name, gender=:gender, birthday=:birthday, start=:start. exist=:exist ) ";

         var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_SELECT)
                .setAny("name", "Cuong")
                .setAny("id", 20)
                .setAny("gender", true)
                .setAny("birhday", Date.valueOf(LocalDate.now()).)
                .setAny("start", Time.valueOf(LocalTime.now()))
                .setAny("exist", Timestamp.valueOf(LocalDateTime.now()));
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }

    private Message createCreateTableMessage(String tableName, Column... column){
        StringBuilder queryBuilder = new StringBuilder("create table ")
                .append(tableName)
                .append(" ( ");
        for (int i = 0; i < column.length; i++){
            queryBuilder.append(column[i].toString());
            queryBuilder.append(",");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(");");
        return Message.ofAny(queryBuilder.toString());
    }
    private Message createCreateTableMessage(String tableName, List<String> columns){
        StringBuilder queryBuilder = new StringBuilder("create table ")
                .append(tableName)
                .append(" ( ");
        for (String column : columns){
            queryBuilder.append(column);
            queryBuilder.append(",");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(");");
        return Message.ofAny(queryBuilder.toString());
    }

    private Message createDropTableMessage(String tableName){
        return Message.ofAny("drop table " + tableName + " ;");
    }

    @Before
    public void initialize(){
        registry = new SimpleRegistry();
        context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        connector = new DefaultConnectorFactory().createConnector("jdbc:mysql/localhost/3306/root/ManhCuong22293/test", context);
        connector.start();
        producer = connector.getProducer().orElseThrow();
    }

    @Test
    public void testSelect() throws IOException, InterruptedException {
        var ok = producer.call(createSelectRequest());
        ok.done(msg -> {
            var list = msg.getPayload().getBody().asArray();
            for (BElement bElement : list) {
                var result = bElement.asObject();
                System.out.println("My name is " + result.get("name") + ". ");
                System.out.println("I'm " + result.get("age") + " years old.");
            }
        });
    }

    @Test
    public void testInsert() throws IOException, InterruptedException {
        var ok = producer.call(createInsertRequest());
        ok.done(msg -> {
            var list = msg.getPayload().getBody().asValue().getInteger();
           Assert.assertEquals(Integer.valueOf(1), list);
        });
        ok.fail(ex -> {
            ex.printStackTrace();
            Assert.assertTrue(false);
        });
    }

    @Test
    public void testDropTable(){
        Message message = createDropTableMessage("test1");
        var ok = producer.call(message);
        ok.done(msg -> {
            System.out.println(msg.toString());
        });
    }

    @Test
    public void testCreateTable(){
        Column id = new Column("id", "int");
        List<String> columns = new ArrayList<>();
        columns.add("id int");
        columns.add("name varchar(25)");
        columns.add("gender boolean");
        columns.add("birthday DATE");
        columns.add("start TIME");
        columns.add("exist TIMESTAMP");
        Message message = createCreateTableMessage("test1", columns);
        var ok = producer.call(message);
        ok.done(msg -> {
            System.out.println(msg.toString());
        });
    }


    @Test
    public void testByte(){
        Byte.valueOf("Cuong");
        System.out.println("OKOKOKO");
    }

    class Column{
        String name;
        String type;
        String option;
        Column(String name, String type){
            this.name = name;
            this.type = type;
            this.option = "";
        }
        Column(String name, String type, String option){
            this.name = name;
            this.type = type;
            this.option = option;
        }

        @Override
        public String toString() {
            return this.name + " " + this.type + " " + this.option;
        }
    }

}
