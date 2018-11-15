package io.gridgo.connector.netty4.impl;

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

	public DefaultNetty4Receiver(ConnectorContext context, @NonNull Netty4SocketClient socketClient) {
		super(context);
		this.socketClient = socketClient;
	}

	@Getter(AccessLevel.PROTECTED)
	private Function<Throwable, Message> failureHandler;

	@Override
	public DefaultNetty4Receiver setFailureHandler(Function<Throwable, Message> failureHandler) {
		this.failureHandler = failureHandler;
		return this;
	}

	@Override
	protected void onStart() {
		this.socketClient.setChannelCloseCallback(this::onConnectionClosed);
		this.socketClient.setChannelOpenCallback(this::onConnectionOpened);
		this.socketClient.setReceiveCallback(this::onReceive);
		this.socketClient.setFailureHandler(this::onFailure);
	}

	private void onFailure(Throwable cause) {
		if (this.failureHandler != null) {
			this.failureHandler.apply(cause);
		} else {
			getLogger().error("Receiver error: ", cause);
		}
	}

	@Override
	protected void onStop() {
		System.out.println(this.socketClient.getClass().getSimpleName() + " --> close the client receiver...");
		this.socketClient.setChannelCloseCallback(null);
		this.socketClient.setChannelOpenCallback(null);
		this.socketClient.setReceiveCallback(null);
		this.socketClient.setFailureHandler(null);

		this.socketClient = null;
	}

	protected Deferred<Message, Exception> createDeferred() {
		return new CompletableDeferredObject<>();
	}

	private void publishMessage(Message message) {
		Deferred<Message, Exception> deferred = this.createDeferred();
		deferred.promise().fail(this::onFailure);
		this.publish(message, deferred);
	}

	private void onConnectionOpened() {
		this.publishMessage(this.createMessage().addMisc("socketMessageType", "open"));
	}

	private void onReceive(BElement element) {
		this.publishMessage(this.parseMessage(element).addMisc("socketMessageType", "message"));
	}

	private void onConnectionClosed() {
		this.publishMessage(this.createMessage().addMisc("socketMessageType", "close"));
	}

	@Override
	protected String generateName() {
		return null;
	}
}
