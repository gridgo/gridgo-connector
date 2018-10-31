package io.gridgo.socket.zmq;

import org.zeromq.ZMQ;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.socket.impl.AbstractBrokerSocketFactory;
import lombok.AccessLevel;
import lombok.Getter;
import net.jodah.failsafe.internal.util.Assert;

public class ZMQSocketFactory extends AbstractBrokerSocketFactory {

	@Getter(AccessLevel.PROTECTED)
	private final ZMQ.Context ctx;

	public ZMQSocketFactory() {
		this(1);
	}

	public ZMQSocketFactory(int ioThreads) {
		this.ctx = ZMQ.context(ioThreads);
	}

	private BrokerlessSocket createZmqSocket(int type) {
		return new ZMQSocket(ctx.socket(type));
	}

	protected BrokerlessSocket createCustomSocket(String type) {
		Assert.notNull(type, "Socket type");
		switch (type.toLowerCase()) {
		case "router":
			return createZmqSocket(ZMQ.ROUTER);
		case "dealer":
			return createZmqSocket(ZMQ.DEALER);
		}
		return null;
	}

	protected BrokerlessSocket createPullSocket() {
		return createZmqSocket(ZMQ.PULL);
	}

	protected BrokerlessSocket createPushSocket() {
		return createZmqSocket(ZMQ.PUSH);
	}

	protected BrokerlessSocket createPubSocket() {
		return createZmqSocket(ZMQ.PUB);
	}

	protected BrokerlessSocket createSubSocket() {
		return createZmqSocket(ZMQ.SUB);
	}

	protected BrokerlessSocket createReqSocket() {
		return createZmqSocket(ZMQ.REQ);
	}

	protected BrokerlessSocket createRepSocket() {
		return createZmqSocket(ZMQ.REP);
	}

	protected BrokerlessSocket createPairSocket() {
		return createZmqSocket(ZMQ.PAIR);
	}
}
