package io.gridgo.connector.netty4.impl;

import java.io.File;
import java.util.function.Function;

import javax.net.ssl.SSLContext;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Responder;
import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.netty4.Netty4Server;
import io.gridgo.connector.netty4.exceptions.UnsupportedTransportException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.InvalidParamException;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.socket.netty4.raw.tcp.Netty4TCPServer;
import io.gridgo.socket.netty4.utils.KeyStoreType;
import io.gridgo.socket.netty4.utils.SSLContextRegistry;
import io.gridgo.socket.netty4.ws.Netty4Websocket;
import io.gridgo.socket.netty4.ws.Netty4WebsocketServer;
import io.gridgo.utils.support.HostAndPort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractNetty4Server extends AbstractHasResponderConsumer implements Netty4Server {

    @Getter(AccessLevel.PROTECTED)
    private final Netty4Transport transport;

    @Getter(AccessLevel.PROTECTED)
    private final HostAndPort host;

    @Getter(AccessLevel.PROTECTED)
    private final BObject options;

    @Getter(AccessLevel.PROTECTED)
    private final String path;

    @Getter(AccessLevel.PROTECTED)
    private Netty4SocketServer socketServer;

    @Getter(AccessLevel.PROTECTED)
    private Function<Throwable, Message> failureHandler;

    protected AbstractNetty4Server(@NonNull ConnectorContext context, @NonNull Netty4Transport transport, @NonNull HostAndPort host, @NonNull String path,
            @NonNull BObject options) {
        super(context);
        this.transport = transport;
        this.host = host;
        this.path = path;
        this.options = options;

        initSocketServer();
    }

    protected Deferred<Message, Exception> createDeferred() {
        return new CompletableDeferredObject<>();
    }

    protected abstract Responder createResponder();

    protected Netty4SocketServer createSocketServer() {
        switch (this.transport) {
        case TCP:
            return new Netty4TCPServer();
        case WEBSOCKET:
            return new Netty4WebsocketServer();
        case WEBSOCKET_SSL:
            String sslContextName = this.registerSSLContext();
            return new Netty4WebsocketServer(true, sslContextName);
        }
        throw new UnsupportedTransportException("Transport type doesn't supported: " + this.transport);
    }

    protected String registerSSLContext() {
        String sslContextName = this.getOptions().getString("sslContext", null);
        if (sslContextName != null) {
            SSLContext sslContext = (SSLContext) this.getContext().getRegistry().lookupMandatory(sslContextName);
            SSLContextRegistry.getInstance().register(sslContextName, sslContext);
            return sslContextName;
        }

        String keyStoreFilePath = this.getOptions().getString("keyStoreFile", null);
        if (keyStoreFilePath == null) {
            throw new InvalidParamException("Missing both sslContext and keyStoreFilePath param");
        }

        KeyStoreType type = KeyStoreType.forName(this.getOptions().getString("keyStoreType", null));
        if (type == null) {
            throw new InvalidParamException("Missing keyStoreType param");
        }

        String algorithm = this.getOptions().getString("keyStoreAlgorithm", "SunX509");
        if (algorithm == null) {
            throw new InvalidParamException("Missing keyStoreAlgorithm param");
        }

        String protocol = this.getOptions().getString("sslProtocol", "TLS");
        if (protocol == null) {
            throw new InvalidParamException("Missing sslProtocol param");
        }

        String password = this.getOptions().getString("keyStorePassword", null);

        String name = this.getUniqueIdentifier() + "-sslContext";
        SSLContextRegistry.getInstance().register(name, new File(keyStoreFilePath), type, algorithm, protocol, password);

        return name;
    }

    @Override
    protected String generateName() {
        return "consumer." + this.getUniqueIdentifier();
    }

    protected String getUniqueIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append("netty:server:").append(this.transport.name().toLowerCase()).append("://").append(this.host.toIpAndPort());
        if (this.transport == Netty4Transport.WEBSOCKET && this.path != null) {
            sb.append(this.path.startsWith("/") ? "" : "/").append(this.path);
        }
        return sb.toString();
    }

    private void initSocketServer() {
        this.socketServer = this.createSocketServer();

        this.socketServer.applyConfigs(this.options);
        if (this.socketServer instanceof Netty4Websocket) {
            ((Netty4Websocket) this.socketServer).setPath(this.getPath());
        }

        this.setResponder(this.createResponder());
    }

    protected abstract void onConnectionClose(String channelId);

    protected abstract void onConnectionOpen(String channelId);

    protected final void onFailure(Throwable cause) {
        if (this.failureHandler != null) {
            this.failureHandler.apply(cause);
        } else {
            getLogger().error("Netty4 consumer error", cause);
        }
    }

    protected abstract void onReceive(String channelId, BElement data);

    @Override
    protected void onStart() {
        this.socketServer.setChannelOpenCallback(this::onConnectionOpen);
        this.socketServer.setChannelCloseCallback(this::onConnectionClose);
        this.socketServer.setReceiveCallback(this::onReceive);
        this.socketServer.setFailureHandler(this::onFailure);

        this.socketServer.bind(host);
    }

    @Override
    protected void onStop() {
        this.getResponder().stop();
        this.setResponder(null);

        this.socketServer.stop();
        this.socketServer.setChannelCloseCallback(null);
        this.socketServer.setChannelOpenCallback(null);
        this.socketServer.setReceiveCallback(null);
        this.socketServer = null;
    }

    @Override
    public Netty4Server setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

}
