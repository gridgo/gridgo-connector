package io.gridgo.connector.mysql;


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
        dropTable(testUtil);
        createTable(testUtil);
        insert(testUtil);
        select(testUtil);
    }

    private void select(TestUtil testUtil){
        var ok = producer.call(testUtil.createSelectRequest());
        var sqlValues = testUtil.getSqlValues();
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

     private void insert(TestUtil testUtil) {
        var ok = producer.call(testUtil.createInsertRequest());
        ok.done(msg -> {
            var list = msg.getPayload().getBody().asValue().getInteger();
            Assert.assertEquals(Integer.valueOf(1), list);
        });
        ok.fail(ex -> {
            ex.printStackTrace();
            Assert.fail();
        });
    }

    private void dropTable(TestUtil testUtil){
        Message message = testUtil.createDropTableMessage();
        producer.call(message);
    }

    private void createTable(TestUtil testUtil){
        Message message = testUtil.createCreateTableMessage();
        var ok = producer.call(message);
        ok.done(msg -> Assert.assertTrue(true))
            .fail(ex -> {
                ex.printStackTrace();
                Assert.fail();
            });
    }
}
