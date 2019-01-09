import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.mysql.MySQLConstants;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.impl.SimpleRegistry;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MySQLConnectorTest {
    @Test
    public void testSelect() throws Exception {

    }
    String sql = "select name, age from test_table where name=:name and age=:age";
    private Message createFindByIdRequest() {
        var headers = BObject.ofEmpty().setAny(MySQLConstants.OPERATION, MySQLConstants.OPERATION_SELECT)
                .setAny("name", "Cuong")
                .setAny("age", 20);
        return Message.of(Payload.of(headers, BValue.of(sql)));
    }
    @Test
    public void testSimple() throws IOException, InterruptedException {



        var registry = new SimpleRegistry();
        var context = new DefaultConnectorContextBuilder().setRegistry(registry).build();
        var connector = new DefaultConnectorFactory().createConnector("jdbc:localhost/3306/root/ManhCuong22293/test", context);
        connector.start();
        var producer = connector.getProducer().orElseThrow();
         var ok = producer.call(createFindByIdRequest());
         ok.done(msg -> {
             var list = msg.getPayload().getBody().asArray();
             for (BElement bElement : list) {
                 var result = bElement.asObject();
                 System.out.println("My name is " + result.get("name") + ". ");
                 System.out.println("I'm " + result.get("age") + " years old.");
             }
         });
        var exRef = new AtomicReference<>();

//        var callLatch = new CountDownLatch(1);
//        producer.call(createInsertMessages()) //
//                .pipeDone(msg -> producer.call(createInsertMessage())) //
//                .pipeDone(msg -> producer.call(createCountMessage())) //
//                .pipeDone(msg -> {
//                    System.out.println("check count");
//                    long count = msg.getPayload().getBody().asValue().getLong();
//                    if (count == 4)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createFindByIdRequest())) //
//                .pipeDone(msg -> {
//                    System.out.println("check find by id");
//                    var doc = msg.getPayload().getBody().asObject();
//                    if (doc != null)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createFindAllRequest())) //
//                .pipeDone(msg -> {
//                    System.out.println("check find all");
//                    var doc = msg.getPayload().getBody().asArray();
//                    if (doc != null && doc.size() == 3)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createDeleteRequest())) //
//                .pipeDone(msg -> producer.call(createCountMessage())) //
//                .pipeDone(msg -> {
//                    System.out.println("check count");
//                    long count = msg.getPayload().getBody().asValue().getLong();
//                    if (count == 3)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createUpdateRequest())).pipeDone(msg -> producer.call(createFindAllRequest())) //
//                .pipeDone(msg -> {
//                    System.out.println("check find all");
//                    var doc = msg.getPayload().getBody().asArray();
//                    if (doc != null && doc.size() == 1)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createUpdateManyRequest())).pipeDone(msg -> producer.call(createFindAllRequest())) //
//                .pipeDone(msg -> {
//                    System.out.println("check find all");
//                    var doc = msg.getPayload().getBody().asArray();
//                    if (doc != null && doc.size() == 3)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createDeleteManyRequest())) //
//                .pipeDone(msg -> producer.call(createCountMessage())) //
//                .pipeDone(msg -> {
//                    System.out.println("check count");
//                    long count = msg.getPayload().getBody().asValue().getLong();
//                    if (count == 0)
//                        return new SimpleDonePromise<Message, Exception>(msg);
//                    return new SimpleFailurePromise<Message, Exception>(new RuntimeException());
//                }) //
//                .pipeDone(msg -> producer.call(createInsertMessage())) //
//                .done(msg -> callLatch.countDown()).fail(ex -> {
//            exRef.set(ex);
//            latch.countDown();
//        });
//        callLatch.await();
//
//        Assert.assertNull(exRef.get());
//
//        // test perf
//        var failure = new AtomicInteger(0);
//        var perfLatch = new AtomicInteger(NUM_MESSAGES);
//        long perfStarted = System.nanoTime();
//        for (int i = 0; i < NUM_MESSAGES; i++)
//            producer.call(createFindByIdRequest()) //
//                    .always((status, msg, ex) -> perfLatch.decrementAndGet()) //
//                    .fail(ex -> {
//                        ex.printStackTrace();
//                        failure.incrementAndGet();
//                    });
//        while (perfLatch.get() != 0) {
//            Thread.onSpinWait();
//        }
//        long perfElapsed = System.nanoTime() - perfStarted;
//        System.out.println("Failures: " + failure.get());
//        DecimalFormat df = new DecimalFormat("###,###.##");
//        System.out.println("MongoDB findById done, " + NUM_MESSAGES + " messages were transmited in " + df.format(perfElapsed / 1e6) + "ms -> pace: "
//                + df.format(1e9 * NUM_MESSAGES / perfElapsed) + "msg/s");
//
//        connector.stop();
//        mongo.close();
//
//        Assert.assertEquals(0, failure.get());
    }

    @Test
    public void testByte(){
        Byte.valueOf("Cuong");
        System.out.println("OKOKOKO");
    }
}
