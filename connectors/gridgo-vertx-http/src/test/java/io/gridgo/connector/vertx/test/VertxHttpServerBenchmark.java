package io.gridgo.connector.vertx.test;

import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.ThreadUtils;

public class VertxHttpServerBenchmark {

    public static void main(String[] args) {
        var msg = Message.ofAny("helloworld");
        var connector = new DefaultConnectorFactory().createConnector("vertx:http://localhost:8888/?acceptBacklog=10000&method=GET");
        connector.getConsumer().get().subscribe((request, deferred) -> {
            deferred.resolve(msg);
        });
        ThreadUtils.registerShutdownTask(connector::stop);
        connector.start();
    }
}
