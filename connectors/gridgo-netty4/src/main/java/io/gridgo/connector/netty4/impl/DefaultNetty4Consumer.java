package io.gridgo.connector.netty4.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Responder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public class DefaultNetty4Consumer extends AbstractNetty4Consumer {

	public DefaultNetty4Consumer(ConnectorContext context, Netty4Transport transport, HostAndPort host, String path,
			BObject options) {
		super(context, transport, host, path, options);
	}

	@Override
	protected Responder createResponder() {
		return new DefaultNetty4Responder(this.getContext(), getSocketServer());
	}

	protected void onConnectionClose(long routingId) {
		System.out.println("Connection closed on " + routingId);
	}

	protected void onConnectionOpen(long routingId) {
		System.out.println("Connection opened, id: " + routingId);
	}

	protected void onReceive(long routingId, BElement data) {
		Message message = MessageParser.DEFAULT.parse(data).setRoutingIdFromAny(routingId);
		Deferred<Message, Exception> deferred = this.createDeferred();
		deferred.promise().fail(this::onFailure);
		this.publish(message, deferred);
	}
}
