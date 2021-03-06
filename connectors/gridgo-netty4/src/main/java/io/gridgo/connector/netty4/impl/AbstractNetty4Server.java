package io.gridgo.connector.netty4.impl;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Responder;
import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.netty4.Netty4Server;
import io.gridgo.connector.netty4.exceptions.UnsupportedTransportException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.socket.netty4.raw.tcp.Netty4TCPServer;
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
        }
        throw new UnsupportedTransportException("Transport type doesn't supported: " + this.transport);
    }

    @Override
    protected String generateName() {
        return "consumer." + this.getUniqueIdentifier();
    }

    protected String getUniqueIdentifier() {
        return "netty:server:" + this.transport.name().toLowerCase() + "://" + this.host.toIpAndPort();
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
