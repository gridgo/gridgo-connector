package io.gridgo.jetty.test.file;

import java.io.File;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.ThreadUtils;

public class TestFileServer {
    private static final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

//	private final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: provide argument 1 as <endpoint>, example: http://localhost:8080/*");
            return;
        }

        String endpoint = args[0];
        String directory = args.length > 1 ? args[1] : "test-resources";

        final var app = new TestFileServer(endpoint, directory);
        ThreadUtils.registerShutdownTask(() -> {
            app.stop();
        });
        app.start();
    }
    private Connector connector;
    private Consumer consumer;

    private Producer responder;

    private File rootDir;

    private TestFileServer(String endpoint, String directory) {
        System.out.println("Http server listen on: " + endpoint + ", root dir: " + directory);

        this.rootDir = new File(directory);

        if (!rootDir.exists()) {
            throw new RuntimeException("Root directory at: " + rootDir.getAbsolutePath() + " doesn't exist");
        }

        ConnectorContext connectorContext = new DefaultConnectorContextBuilder() //
                                                                                 // .setCallbackInvokerStrategy(new
                                                                                 // ExecutorExecutionStrategy(executor))
                                                                                 // //
                                                                                .setExceptionHandler((ex) -> {
                                                                                    ex.printStackTrace();
                                                                                }) //
                                                                                .build();

        connector = resolver.resolve("jetty:" + endpoint, connectorContext);
    }

    private void onRequest(Message message) {
        BObject headers = BObject.ofEmpty();
        BElement body = null;

        String pathInfo = message.headers().getString(HttpCommonConstants.PATH_INFO);
        if (pathInfo != null) {
            if (pathInfo.equalsIgnoreCase("/upload")) {
                System.out.println("got request: " + message.getPayload().toBArray());
            } else {
                File file = new File(rootDir, pathInfo);
                if (file.exists() && file.isFile()) {
                    body = BReference.of(file);
                }
            }
        }

        if (body == null) {
            headers.setAny(HttpCommonConstants.HEADER_STATUS, 404);
            body = BValue.of("Not found");
        }

        Payload payload = Payload.of(headers, body);
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
