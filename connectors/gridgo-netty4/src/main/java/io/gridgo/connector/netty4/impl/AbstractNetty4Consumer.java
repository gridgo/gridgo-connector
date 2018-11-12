package io.gridgo.connector.netty4.impl;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.netty4.Netty4Consumer;
import io.gridgo.connector.netty4.exceptions.UnsupportedTransportException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.socket.netty4.raw.tcp.Netty4TCPServer;
import io.gridgo.socket.netty4.ws.Netty4WebsocketServer;
import io.gridgo.utils.support.HostAndPort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractNetty4Consumer extends AbstractHasResponderConsumer implements Netty4Consumer {

	@Getter(AccessLevel.PROTECTED)
	private final Netty4Transport transport;

	@Getter(AccessLevel.PROTECTED)
	private final HostAndPort host;

	@Getter(AccessLevel.PROTECTED)
	private final BObject options;

	private Netty4SocketServer socketServer;

	protected AbstractNetty4Consumer(@NonNull ConnectorContext context, @NonNull Netty4Transport transport,
			@NonNull HostAndPort host, @NonNull BObject options) {
		super(context);
		this.transport = transport;
		this.host = host;
		this.options = options;
	}

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
	protected void onStart() {
		this.socketServer = this.createSocketServer();
		this.socketServer.applyConfigs(this.options);
		this.socketServer.setChannelOpenCallback(this::onConnectionOpen);
		this.socketServer.setChannelCloseCallback(this::onConnectionClose);
		this.socketServer.setReceiveCallback(this::onReceive);
		this.setResponder(new DefaultNetty4Responder(this.getContext(), socketServer));
		this.socketServer.bind(host);
	}

	protected void onConnectionClose(long routingId) {
		System.out.println("Connection closed on " + routingId);
	}

	protected void onConnectionOpen(long routingId) {
		System.out.println("Connection opened, id: " + routingId);
	}

	protected void onReceive(long routingId, BElement data) {
		Message message = MessageParser.DEFAULT.parse(data).setRoutingIdFromAny(routingId);
		this.publish(message, null);
	}

	@Override
	protected void onStop() {
		this.socketServer.stop();
		this.socketServer.setChannelCloseCallback(null);
		this.socketServer.setChannelOpenCallback(null);
		this.socketServer.setReceiveCallback(null);
		this.socketServer = null;
		this.setResponder(null);
	}
}
