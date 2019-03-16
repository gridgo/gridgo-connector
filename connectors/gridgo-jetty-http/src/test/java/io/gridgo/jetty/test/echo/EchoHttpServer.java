package io.gridgo.jetty.test.echo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.ThreadUtils;

public class EchoHttpServer {

    private static final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: provide argument 1 as <endpoint>, example: http://localhost:8080/path");
            return;
        }
        final EchoHttpServer app = new EchoHttpServer(args[0]);
        ThreadUtils.registerShutdownTask(() -> {
            app.stop();
        });
        app.start();
    }

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Connector connector;
    private Consumer consumer;

    private Producer responder;

    private EchoHttpServer(String endpoint) {
        System.out.println("Http server listen on: " + endpoint);

        ConnectorContext connectorContext = new DefaultConnectorContextBuilder() //
                                                                                .setCallbackInvokerStrategy(new ExecutorExecutionStrategy(executor)) //
                                                                                .setExceptionHandler((ex) -> {
                                                                                    ex.printStackTrace();
                                                                                }) //
                                                                                .build();

        connector = resolver.resolve("jetty:" + endpoint, connectorContext);
    }

    private void onRequest(Message message) {
        System.out.println("Got message payload: " + message.getPayload().toBArray());

        BElement body = message.getPayload().toBArray();
        Payload payload = Payload.of(body);
        Message response = Message.of(payload).setRoutingIdFromAny(message.getRoutingId().get());

        this.responder.send(response);
    }

    private void start() {
        connector.start();

        consumer = connector.getConsumer().get();
        responder = connector.getProducer().get();

        consumer.subscribe(this::onRequest);
    }

    private void stop() {
        try {
            this.consumer.clearSubscribers();
            this.connector.stop();
        } finally {
            this.connector = null;
            this.consumer = null;
            this.responder = null;
        }
    }
}
