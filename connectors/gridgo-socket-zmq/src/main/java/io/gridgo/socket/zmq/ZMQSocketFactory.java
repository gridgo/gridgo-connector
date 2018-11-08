package io.gridgo.socket.zmq;

import org.zeromq.ZMQ;

import io.gridgo.socket.Socket;
import io.gridgo.socket.impl.BaseSocketFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public class ZMQSocketFactory extends BaseSocketFactory {

	@Getter(AccessLevel.PROTECTED)
	private final ZMQ.Context ctx;

	public ZMQSocketFactory() {
		this(1);
	}

	public ZMQSocketFactory(int ioThreads) {
		this.ctx = ZMQ.context(ioThreads);
	}

	private Socket createZmqSocket(int type) {
		return new ZMQSocket(ctx.socket(type));
	}

	protected Socket createCustomSocket(final @NonNull String type) {
		switch (type.toLowerCase()) {
		case "router":
			return createZmqSocket(ZMQ.ROUTER);
		case "dealer":
			return createZmqSocket(ZMQ.DEALER);
		}
		return null;
	}

	protected Socket createPullSocket() {
		return createZmqSocket(ZMQ.PULL);
	}

	protected Socket createPushSocket() {
		return createZmqSocket(ZMQ.PUSH);
	}

	protected Socket createPubSocket() {
		return createZmqSocket(ZMQ.PUB);
	}

	protected Socket createSubSocket() {
		return createZmqSocket(ZMQ.SUB);
	}

	protected Socket createReqSocket() {
		return createZmqSocket(ZMQ.REQ);
	}

	protected Socket createRepSocket() {
		return createZmqSocket(ZMQ.REP);
	}

	protected Socket createPairSocket() {
		return createZmqSocket(ZMQ.PAIR);
	}
}
