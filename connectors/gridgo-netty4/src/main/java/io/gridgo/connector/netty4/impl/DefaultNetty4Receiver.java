package io.gridgo.connector.netty4.impl;

import java.util.function.Function;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractReceiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
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
		this.socketClient.setChannelCloseCallback(null);
		this.socketClient.setChannelOpenCallback(null);
		this.socketClient.setReceiveCallback(null);
		this.socketClient.setFailureHandler(null);

		this.socketClient = null;
	}

	private void onConnectionOpened() {

	}

	private void onReceive(BElement element) {
		this.publish(MessageParser.DEFAULT.parse(element), null);
	}

	private void onConnectionClosed() {

	}

	@Override
	protected String generateName() {
		return null;
	}
}
