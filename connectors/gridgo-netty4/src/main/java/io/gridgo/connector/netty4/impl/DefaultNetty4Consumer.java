package io.gridgo.connector.netty4.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Responder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
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

	private void publishMessage(Message message) {
		Deferred<Message, Exception> deferred = this.createDeferred();
		deferred.promise().fail(this::onFailure);
		this.publish(message, deferred);
	}

	protected void onConnectionClose(long routingId) {
		Message message = this.createMessage().setRoutingIdFromAny(routingId);
		message.addMisc("socketMessageType", "close");
		publishMessage(message);
	}

	protected void onConnectionOpen(long routingId) {
		Message message = this.createMessage().setRoutingIdFromAny(routingId);
		message.addMisc("socketMessageType", "open");
		publishMessage(message);
	}

	protected void onReceive(long routingId, BElement data) {
		Message message = this.parseMessage(data).setRoutingIdFromAny(routingId);
		message.addMisc("socketMessageType", "message");
		publishMessage(message);
	}
}
