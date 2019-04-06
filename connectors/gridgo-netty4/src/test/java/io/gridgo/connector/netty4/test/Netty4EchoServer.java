package io.gridgo.connector.netty4.test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.utils.ThreadUtils;

public class Netty4EchoServer {

    private final static ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    public static void main(String[] args) {
        String transport = "ws";
        String host = "0.0.0.0:8889";
        String path = "websocket";

        if (args.length > 0) {
            transport = args[0];
            if (args.length > 1) {
                host = args[1];
                if (args.length > 2) {
                    path = args[2];
                }
            }
        }

        String endpoint = "netty4:server:" + transport + "://" + host + (path == null ? "" : ("/" + path));
        Connector serverConnector = RESOLVER.resolve(endpoint);

        // server side
        final Consumer serverConsumer = serverConnector.getConsumer().get();
        final Producer serverResponder = serverConnector.getProducer().get();

        serverConsumer.subscribe((msg) -> {
            String socketMessageType = (String) msg.getMisc().get("socketMessageType");
            switch (socketMessageType) {
            case "open":
                System.out.println("socket open, routing id: " + msg.getRoutingId().get());
                break;
            case "close":
                System.out.println("socket closed, routing id: " + msg.getRoutingId().get());
                break;
            default:
                System.out.println("got msg with misc: " + BObject.wrap(msg.getMisc()));
                serverResponder.send(msg);
            }
        });

        ((FailureHandlerAware<?>) serverConsumer).setFailureHandler((cause) -> {
            cause.printStackTrace();
            return null;
        });

        serverConnector.start();
        System.out.println("Echo server started at: " + endpoint);

        ThreadUtils.registerShutdownTask(() -> {
            serverConnector.stop();
        });
    }

}
