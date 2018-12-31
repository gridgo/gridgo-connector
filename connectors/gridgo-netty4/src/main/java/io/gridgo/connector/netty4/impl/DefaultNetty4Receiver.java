package io.gridgo.connector.netty4.impl;

import static io.gridgo.connector.netty4.Netty4Constant.MISC_SOCKET_MSG_TYPE;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractReceiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.netty4.Netty4SocketClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public class DefaultNetty4Receiver extends AbstractReceiver implements FailureHandlerAware<DefaultNetty4Receiver> {

    private Netty4SocketClient socketClient;

    private final String uniqueIdentifier;

    @Getter(AccessLevel.PROTECTED)
    private Function<Throwable, Message> failureHandler;

    public DefaultNetty4Receiver(ConnectorContext context, @NonNull Netty4SocketClient socketClient, @NonNull String uniqueIdentifier) {
        super(context);
        this.socketClient = socketClient;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    protected Deferred<Message, Exception> createDeferred() {
        return new CompletableDeferredObject<>();
    }

    @Override
    protected String generateName() {
        return "consumer." + this.uniqueIdentifier;
    }

    private void onConnectionClosed() {
        this.publishMessage(this.createMessage().addMisc(MISC_SOCKET_MSG_TYPE, "close"));

        this.socketClient.setChannelCloseCallback(null);
        this.socketClient.setFailureHandler(null);
    }

    private void onConnectionOpened() {
        this.publishMessage(this.createMessage().addMisc(MISC_SOCKET_MSG_TYPE, "open"));
    }

    private void onFailure(Throwable cause) {
        if (!this.socketClient.isRunning()) {
            this.socketClient.setChannelCloseCallback(null);
            this.socketClient.setFailureHandler(null);
        }

        if (this.failureHandler != null) {
            this.failureHandler.apply(cause);
        } else {
            getLogger().error("Receiver error: ", cause);
        }
    }

    private void onReceive(BElement element) {
        this.publishMessage(this.parseMessage(element).addMisc(MISC_SOCKET_MSG_TYPE, "message"));
    }

    @Override
    protected void onStart() {
        this.socketClient.setChannelCloseCallback(this::onConnectionClosed);
        this.socketClient.setChannelOpenCallback(this::onConnectionOpened);
        this.socketClient.setReceiveCallback(this::onReceive);
        this.socketClient.setFailureHandler(this::onFailure);
    }

    @Override
    protected void onStop() {
        this.socketClient.setChannelOpenCallback(null);
        this.socketClient.setReceiveCallback(null);
    }

    private void publishMessage(Message message) {
        Deferred<Message, Exception> deferred = this.createDeferred();
        deferred.promise().fail(this::onFailure);
        this.publish(message, deferred);
    }

    @Override
    public DefaultNetty4Receiver setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }
}
