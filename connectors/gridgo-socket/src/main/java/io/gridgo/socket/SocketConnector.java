package io.gridgo.socket;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.connector.support.exceptions.MalformedEndpointException;

/**
 * The sub-class must annotated by {@link io.gridgo.connector.ConnectorResolver
 * ConnectorResolver} which syntax has some placeholders as:
 * <ol>
 * <li><b>{type}</b>: push, pull, pub, sub, pair</li>
 * <li><b>{transport}</b>: tcp, pgm, epgm, inproc, ipc</li>
 * <li><b>[{role}]</b>: for <b>'pair'</b> pattern, indicate the socket will be
 * <i>active</i> or <i>passive</i></li>
 * <li><b>{host}</b>: allow ipv4, ipv6 (with bracket [])</li>
 * <li><b>[{interface}]</b>: use for multicast transport types (pgm or epgm)
 * </li>
 * <li><b>{port}</b>: port (to bind-on or connect-to)</li>
 * </ol>
 *
 * @author bachden
 *
 */
public class SocketConnector extends AbstractConnector implements Connector {

    public static final Set<String> MULTICAST_TRANSPORTS = //
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("pgm", "epgm")));

    public static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

    public static final int DEFAULT_RINGBUFFER_SIZE = 1024;

    public static final int DEFAULT_MAX_BATCH_SIZE = 1000;

    private String address;

    private SocketOptions options;

    private final SocketFactory factory;

    private boolean batchingEnabled = false;

    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private int ringBufferSize = DEFAULT_RINGBUFFER_SIZE;

    protected SocketConnector(SocketFactory factory) {
        this.factory = factory;
    }

    private void initConsumerAndProducer() {
        Producer p = null;
        Consumer c = null;
        switch (this.options.getType().toLowerCase()) {
        case "push":
        case "pub":
            p = SocketProducer.of(getContext(), factory, options, address, bufferSize, ringBufferSize, batchingEnabled, maxBatchSize);
            break;
        case "pull":
        case "sub":
            c = SocketConsumer.of(getContext(), factory, options, address, bufferSize);
            break;
        case "pair":
            String role = this.getPlaceholder("role");
            if (role == null || role.isBlank()) {
                throw new MalformedEndpointException("Pair socket require socket role (connect or bind)");
            }
            switch (role.trim().toLowerCase()) {
            case "connect":
                p = SocketProducer.of(getContext(), factory, options, address, bufferSize, ringBufferSize, batchingEnabled, maxBatchSize);
                p.start();
                c = ((HasReceiver) p).getReceiver();
                break;
            case "bind":
                c = SocketConsumer.of(getContext(), factory, options, address, bufferSize);
                c.start();
                p = ((HasResponder) c).getResponder();
                break;
            default:
                throw new InvalidPlaceholderException("Invalid pair socket role, expected 'connect' or 'bind'");
            }
            break;
        default:
        }
        this.producer = Optional.ofNullable(p);
        this.consumer = Optional.ofNullable(c);
    }

    @Override
    public void onInit() {
        ConnectorConfig config = getConnectorConfig();
        String type = config.getPlaceholders().getProperty(SocketConstants.TYPE);
        String transport = config.getPlaceholders().getProperty(SocketConstants.TRANSPORT);
        String host = config.getPlaceholders().getProperty(SocketConstants.HOST);
        String portPlaceholder = config.getPlaceholders().getProperty(SocketConstants.PORT);
        int port = portPlaceholder != null ? Integer.parseInt(portPlaceholder) : 0;

        String nic = null;
        if (MULTICAST_TRANSPORTS.contains(transport.trim().toLowerCase())) {
            nic = getPlaceholder("interface");
        }

        this.address = transport + "://" + ((nic == null || nic.isBlank()) ? "" : (nic + ";")) + host + (port > 0 ? (":" + port) : "");

        this.batchingEnabled = Boolean.valueOf(config.getParameters().getOrDefault(SocketConstants.BATCHING_ENABLED, this.batchingEnabled).toString());

        this.maxBatchSize = Integer.valueOf(config.getParameters().getOrDefault(SocketConstants.MAX_BATCH_SIZE, this.maxBatchSize).toString());

        this.bufferSize = Integer.valueOf(config.getParameters().getOrDefault(SocketConstants.BUFFER_SIZE, this.bufferSize).toString());

        this.ringBufferSize = Integer.valueOf(config.getParameters().getOrDefault(SocketConstants.RING_BUFFER_SIZE, this.ringBufferSize).toString());

        this.options = new SocketOptions();
        this.options.setType(type);
        this.options.getConfig().putAll(config.getParameters());

        this.initConsumerAndProducer();
    }
}
