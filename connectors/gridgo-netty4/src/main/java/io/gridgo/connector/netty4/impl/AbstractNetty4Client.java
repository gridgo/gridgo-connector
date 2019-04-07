package io.gridgo.connector.netty4.impl;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.AbstractHasReceiverProducer;
import io.gridgo.connector.netty4.Netty4Client;
import io.gridgo.connector.netty4.exceptions.UnsupportedTransportException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.netty4.Netty4SocketClient;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.socket.netty4.raw.tcp.Netty4TCPClient;
import io.gridgo.socket.netty4.ws.Netty4Websocket;
import io.gridgo.socket.netty4.ws.Netty4WebsocketClient;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractNetty4Client extends AbstractHasReceiverProducer implements Netty4Client {

    @Getter(AccessLevel.PROTECTED)
    private final Netty4Transport transport;

    @Getter(AccessLevel.PROTECTED)
    private final HostAndPort host;

    @Getter(AccessLevel.PROTECTED)
    private final BObject options;

    @Getter(AccessLevel.PROTECTED)
    private Netty4SocketClient socketClient;

    @Getter(AccessLevel.PROTECTED)
    private final String path;

    private Function<Throwable, Message> failureHandler;

    protected AbstractNetty4Client(@NonNull ConnectorContext context, @NonNull Netty4Transport transport, @NonNull HostAndPort host, String path,
            @NonNull BObject options) {
        super(context);
        this.transport = transport;
        this.host = host;
        this.options = options;
        this.path = path;

        initSocketClient();
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        throw new UnsupportedOperationException("Cannot make a call on netty4 producer");
    }

    protected abstract Receiver createReceiver();

    protected Netty4SocketClient createSocketClient() {
        switch (transport) {
        case TCP:
            return new Netty4TCPClient();
        case WEBSOCKET:
            return new Netty4WebsocketClient();
        case WEBSOCKET_SSL:
            return new Netty4WebsocketClient(true);
        }
        throw new UnsupportedTransportException("Transport type " + transport + " doesn't supported");
    }

    @Override
    protected String generateName() {
        return "producer." + this.getUniqueIdentifier();
    }

    protected String getUniqueIdentifier() {
        return "netty:client:" + this.transport.name().toLowerCase() + "://" + this.host.toIpAndPort();
    }

    private void initSocketClient() {
        this.socketClient = this.createSocketClient();
        this.socketClient.applyConfigs(this.options);
        if (this.socketClient instanceof Netty4Websocket) {
            ((Netty4Websocket) this.socketClient).setPath(getPath());
        }

        this.setReceiver(this.createReceiver());
    }

    @Override
    public boolean isCallSupported() {
        return false;
    }

    private void onSocketFailure(Throwable cause) {
        if (this.failureHandler != null) {
            this.failureHandler.apply(cause);
        }
    }

    @Override
    protected void onStart() {
        this.socketClient.setFailureHandler(this::onSocketFailure);
        this.socketClient.connect(this.host);
    }

    @Override
    protected void onStop() {
//		System.out.println("Close socket client...");
        this.socketClient.stop();
        this.socketClient = null;

//		System.out.println("Close receiver...");
        this.getReceiver().stop();
        this.setReceiver(null);
    }

    @Override
    public void send(Message message) {
        if (!this.isStarted()) {
            return;
        }
        Payload payload = message.getPayload();
        BArray data = BArray.ofSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody());
        this.socketClient.send(data);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(@NonNull Message message) {
        if (!this.isStarted()) {
            return null;
        }

        Deferred<Message, Exception> deferred = new AsyncDeferredObject<>();
        Payload payload = message.getPayload();
        BArray data = BArray.ofSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody());

        try {
            ChannelFuture future = this.socketClient.send(data);
            future.addListener(new GenericFutureListener<Future<? super Void>>() {

                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    deferred.resolve(null);
                }
            });
        } catch (Exception e) {
            deferred.reject(e);
        }

        return deferred.promise();
    }

    @Override
    public Netty4Client setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }
}
