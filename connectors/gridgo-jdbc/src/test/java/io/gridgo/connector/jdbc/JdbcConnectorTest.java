package io.gridgo.connector.jdbc;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.impl.SimpleRegistry;
import org.jdbi.v3.core.ConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import snaq.db.ConnectionPool;
import java.math.BigDecimal;

public class JdbcConnectorTest {

    private Registry registry;
    private ConnectorContext context;
    private Connector connector;
    private Producer producer;

    @Before
    public void initialize(){
        var pool = new ConnectionPool("local", 5, 15, 0, 180, "jdbc:mysql://localhost:3306/test", "root", "");
        registry = new SimpleRegistry().register("sonaq", (ConnectionFactory)pool::getConnection);
        context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        connector = new DefaultConnectorFactory().createConnector("jdbc:mysql://localhost:3306/test?user=root&pool=sonaq", context);
        connector.start();
        producer = connector.getProducer().orElseThrow();
    }

    @Test
    public void testSelect() {
        TestUtil testUtil = new TestUtil("testSelect");
        try {
            dropTable(testUtil);
            createTable(testUtil);
            insert(testUtil);
            select(testUtil);
        }catch (Exception ex){
            ex.printStackTrace();
            Assert.fail();
        }
    }

    private void select(TestUtil testUtil) {
        var sqlValues = testUtil.getSqlValues();
        try {
            Message msg = producer.call(testUtil.createSelectRequest()).get();
            var list = msg.getPayload().getBody().asArray();
            for (BElement bElement : list) {
                var result = bElement.asObject();
                Assert.assertEquals(sqlValues.get("integertest"), result.getInteger("integertest"));
                Assert.assertEquals(sqlValues.get("stringtest"), result.getString("stringtest"));
                Assert.assertEquals(sqlValues.get("bigdecimaltest"),
                        result.get("bigdecimaltest").asValue().getDataAs(BigDecimal.class));
                Assert.assertEquals(sqlValues.get("booleantest"), result.getBoolean("booleantest"));
                Assert.assertEquals(sqlValues.get("datetest"), result.get("datetest").asReference().getReference());
                Assert.assertEquals(sqlValues.get("timetest"), result.get("timetest").asReference().getReference());
                Assert.assertEquals(sqlValues.get("timestamptest"),
                        result.get("timestamptest").asReference().getReference());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void insert(TestUtil testUtil) {
        var ok = producer.call(testUtil.createInsertRequest());
        try {
            Message message = ok.get();
            Assert.assertEquals(Integer.valueOf(1), message.body().asValue().getInteger());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void dropTable(TestUtil testUtil) {
        Message message = testUtil.createDropTableMessage();
        try {
            producer.call(message).get();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void createTable(TestUtil testUtil) {
        Message message = testUtil.createCreateTableMessage();
        try {
            producer.call(message).get();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
