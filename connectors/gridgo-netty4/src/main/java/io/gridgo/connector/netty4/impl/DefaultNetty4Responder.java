package io.gridgo.connector.netty4.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

class DefaultNetty4Responder extends AbstractResponder {

	private Netty4SocketServer socketServer;

	DefaultNetty4Responder(ConnectorContext context, Netty4SocketServer socketServer) {
		super(context);
		this.socketServer = socketServer;
	}

	@Override
	protected void send(Message message, Deferred<Message, Exception> deferred) {
		if (!this.isStarted()) {
			return;
		}
		BValue routingId = message.getRoutingId().orElse(BValue.newDefault(-1l));
		ChannelFuture future = this.socketServer.send(routingId.getLong(), message.getPayload().toBArray());
		if (deferred != null) {
			if (future != null) {
				future.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						deferred.resolve(null);
					}
				});
			} else {
				deferred.resolve(null);
			}
		}
	}

	@Override
	protected void onStop() {
		this.socketServer = null;
		super.onStop();
	}

	@Override
	protected String generateName() {
		// TODO Auto-generated method stub
		return null;
	}

}
