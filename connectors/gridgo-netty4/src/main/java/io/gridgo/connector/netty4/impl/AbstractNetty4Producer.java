package io.gridgo.connector.netty4.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractHasReceiverProducer;
import io.gridgo.connector.netty4.Netty4Producer;
import io.gridgo.connector.netty4.exceptions.UnsupportedTransportException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.MessageParser;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.netty4.Netty4SocketClient;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.socket.netty4.raw.tcp.Netty4TCPClient;
import io.gridgo.socket.netty4.ws.Netty4WebsocketClient;
import io.gridgo.utils.support.HostAndPort;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public class AbstractNetty4Producer extends AbstractHasReceiverProducer implements Netty4Producer {

	@Getter(AccessLevel.PROTECTED)
	private final Netty4Transport transport;

	@Getter(AccessLevel.PROTECTED)
	private final HostAndPort host;

	@Getter(AccessLevel.PROTECTED)
	private final BObject options;

	private Netty4SocketClient socketClient;

	protected AbstractNetty4Producer(@NonNull ConnectorContext context, @NonNull Netty4Transport transport,
			@NonNull HostAndPort host, @NonNull BObject options) {
		super(context);
		this.transport = transport;
		this.host = host;
		this.options = options;
	}

	@Override
	public void send(Message message) {
		if (!this.isStarted()) {
			return;
		}
		Payload payload = message.getPayload();
		BArray data = BArray.newFromSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody());
		this.socketClient.send(data);
	}

	@Override
	public Promise<Message, Exception> sendWithAck(@NonNull Message message) {
		if (!this.isStarted()) {
			return null;
		}

		Deferred<Message, Exception> deferred = new AsyncDeferredObject<>();
		Payload payload = message.getPayload();
		BArray data = BArray.newFromSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody());

		try {
			ChannelFuture future = this.socketClient.send(data);
			future.addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					deferred.resolve(null);
				}
			});
		} catch (Exception e) {
			deferred.reject(e);
		}

		return deferred.promise();
	}

	@Override
	public Promise<Message, Exception> call(Message request) {
		throw new UnsupportedOperationException("Cannot make a call on netty4 producer");
	}

	protected void onReceive(BElement element) {
		if (this.getReceiveCallback() != null) {
			this.getReceiveCallback().accept(MessageParser.DEFAULT.parse(element));
		}
	}

	@Override
	protected void onStart() {
		this.socketClient = this.createSocketClient();
		this.socketClient.applyConfigs(this.options);
		this.socketClient.setReceiveCallback(this::onReceive);
		this.socketClient.connect(this.host);
	}

	protected Netty4SocketClient createSocketClient() {
		switch (transport) {
		case TCP:
			return new Netty4TCPClient();
		case WEBSOCKET:
			return new Netty4WebsocketClient();
		}
		throw new UnsupportedTransportException("Transport type " + transport + " doesn't supported");
	}

	@Override
	protected void onStop() {
		this.socketClient.stop();
		this.socketClient.setReceiveCallback(null);
		this.socketClient = null;
	}

	@Override
	protected String generateName() {
		return "producer.netty4." + transport + "." + host;
	}
}
