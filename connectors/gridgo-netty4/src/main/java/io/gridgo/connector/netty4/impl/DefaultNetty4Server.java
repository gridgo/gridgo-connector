package io.gridgo.connector.netty4.impl;

import static io.gridgo.connector.netty4.Netty4Constant.MISC_SOCKET_MSG_TYPE;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.Responder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.support.HostAndPort;

public class DefaultNetty4Server extends AbstractNetty4Server {

    public DefaultNetty4Server(ConnectorContext context, Netty4Transport transport, HostAndPort host, String path, BObject options) {
        super(context, transport, host, path, options);
    }

    @Override
    protected Responder createResponder() {
        return new DefaultNetty4Responder(this.getContext(), getSocketServer(), this.getUniqueIdentifier());
    }

    @Override
    protected void onConnectionClose(String channelId) {
        Message message = this.createMessage().setRoutingIdFromAny(channelId);
        message.addMisc(MISC_SOCKET_MSG_TYPE, "close");
        publishMessage(message);
    }

    @Override
    protected void onConnectionOpen(String channelId) {
        Message message = this.createMessage().setRoutingIdFromAny(channelId);
        message.getMisc().putAll(this.getSocketServer().getChannelDetails(channelId));
        message.addMisc(MISC_SOCKET_MSG_TYPE, "open");
        publishMessage(message);
    }

    @Override
    protected void onReceive(String channelId, BElement data) {
        Message message = this.parseMessage(data).setRoutingIdFromAny(channelId);
        message.getMisc().putAll(this.getSocketServer().getChannelDetails(channelId));
        message.addMisc(MISC_SOCKET_MSG_TYPE, "message");
        publishMessage(message);
    }

    private void publishMessage(Message message) {
        Deferred<Message, Exception> deferred = this.createDeferred();
        deferred.promise().fail(this::onFailure);
        this.publish(message, deferred);
    }
}
