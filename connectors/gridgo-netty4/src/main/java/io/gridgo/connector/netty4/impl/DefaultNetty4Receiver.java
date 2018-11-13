package io.gridgo.connector.netty4.impl;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractReceiver;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.socket.netty4.Netty4SocketClient;
import lombok.NonNull;

public class DefaultNetty4Receiver extends AbstractReceiver {

	private Netty4SocketClient socketClient;

	public DefaultNetty4Receiver(ConnectorContext context, @NonNull Netty4SocketClient socketClient) {
		super(context);
		this.socketClient = socketClient;
	}

	@Override
	protected void onStart() {
		this.socketClient.setChannelCloseCallback(this::onConnectionClosed);
		this.socketClient.setChannelOpenCallback(this::onConnectionOpened);
		this.socketClient.setReceiveCallback(this::onReceive);
	}

	@Override
	protected void onStop() {
		this.socketClient.setChannelCloseCallback(null);
		this.socketClient.setChannelOpenCallback(null);
		this.socketClient.setReceiveCallback(null);
		this.socketClient = null;
	}

	private void onConnectionOpened() {

	}

	private void onReceive(BElement element) {
		this.publish(MessageParser.DEFAULT.parse(element), null);
	}

	private void onConnectionClosed() {

	}
}
