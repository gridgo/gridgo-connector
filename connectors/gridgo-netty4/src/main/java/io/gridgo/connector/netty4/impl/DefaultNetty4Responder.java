package io.gridgo.connector.netty4.impl;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
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

    private final String uniqueIdentifier;

    DefaultNetty4Responder(ConnectorContext context, Netty4SocketServer socketServer, String uniqueIdentifier) {
        super(context);
        this.socketServer = socketServer;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    protected String generateName() {
        return "producer." + this.uniqueIdentifier;
    }

    @Override
    protected void onStop() {
        this.socketServer = null;
        super.onStop();
    }

    @Override
    protected void send(Message message, Deferred<Message, Exception> deferred) {
        if (!this.isStarted()) {
            return;
        }
        String routingId = message.getRoutingId().orElseGet(() -> BValue.of(null)).getString();
        BElement data = message.getPayload() == null ? null : message.getPayload().toBArray();

        ChannelFuture future = this.socketServer.send(routingId, data);
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

}
