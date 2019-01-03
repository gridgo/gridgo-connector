package io.gridgo.connector.netty4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

@ConnectorEndpoint(scheme = "netty4", syntax = "{type}:{transport}://{host}[:{port}][/{path}]")
public class Netty4Connector extends AbstractConnector {

    protected static final Collection<String> ALLOWED_TYPES = Arrays.asList("server", "client");

    private Netty4Transport transport;
    private HostAndPort host;
    private String path;
    private BObject options;
    private String type;

    private Netty4Server consumerServer;
    private Netty4Client producerClient;

    private void initProducerAndConsumer() {
        switch (type) {
        case "server":
            consumerServer = Netty4Server.of(this.getContext(), transport, host, path, options);
            this.consumer = Optional.of(consumerServer);
            this.producer = Optional.of(consumerServer.getResponder());
            break;
        case "client":
            producerClient = Netty4Client.of(this.getContext(), transport, host, path, options);
            this.producer = Optional.of(producerClient);
            this.consumer = Optional.of(producerClient.getReceiver());
            break;
        default:
        }
    }

    @Override
    protected void onInit() {
        final ConnectorConfig config = getConnectorConfig();
        type = (String) config.getPlaceholders().getOrDefault("type", null);
        if (type == null || !ALLOWED_TYPES.contains(type.trim().toLowerCase())) {
            throw new InvalidPlaceholderException("type must be provided and is one of " + ALLOWED_TYPES);
        }

        String transportName = (String) config.getPlaceholders().getOrDefault("transport", null);

        transport = Netty4Transport.fromName(transportName);
        if (transport == null) {
            throw new InvalidPlaceholderException("transport must be provided and is one of " + Netty4Transport.values());
        }

        String hostStr = (String) config.getPlaceholders().getOrDefault("host", "localhost");
        if (hostStr == null) {
            throw new InvalidPlaceholderException("Host must be provided by ip, domain name or interface name");
        }

        int port = Integer.parseInt((String) config.getPlaceholders().getOrDefault("port", "0"));

        this.host = HostAndPort.newInstance(hostStr, port);
        this.options = BObject.of(config.getParameters());
        this.path = (String) config.getPlaceholders().getProperty("path", "websocket");

        initProducerAndConsumer();
    }
}
