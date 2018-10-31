package io.gridgo.socket.impl;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.socket.Configurable;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.utils.helper.Assert;

public abstract class AbstractBrokerSocketFactory extends AbstractSocketFactory {

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends Socket<?>> T createSocket(SocketOptions options) {
		Assert.notNull(options, "socket options");
		Assert.notNull(options.getType(), "socket type");

		Socket<?> socket = this.createCustomSocket(options.getType());

		if (socket == null) {
			switch (options.getType().toLowerCase()) {
			case "push":
				socket = this.createPushSocket();
				break;
			case "pull":
				socket = this.createPullSocket();
				break;
			case "req":
			case "request":
				socket = this.createRepSocket();
				break;
			case "rep":
			case "reply":
				socket = this.createRepSocket();
				break;
			case "pub":
				socket = this.createPubSocket();
				break;
			case "sub":
				socket = this.createSubSocket();
				break;
			case "pair":
				socket = this.createPairSocket();
				break;
			}
		}

		if (socket != null && socket instanceof Configurable) {
			((Configurable) socket).applyConfig(options.getConfig());
			return (T) socket;
		}

		throw new IllegalArgumentException("Socket type " + options.getType() + " is not supported");
	}

	protected BrokerlessSocket createCustomSocket(String type) {
		return null;
	}

	protected BrokerlessSocket createPullSocket() {
		return null;
	}

	protected BrokerlessSocket createPushSocket() {
		return null;
	}

	protected BrokerlessSocket createPubSocket() {
		return null;
	}

	protected BrokerlessSocket createSubSocket() {
		return null;
	}

	protected BrokerlessSocket createReqSocket() {
		return null;
	}

	protected BrokerlessSocket createRepSocket() {
		return null;
	}

	protected BrokerlessSocket createPairSocket() {
		return null;
	}

}
