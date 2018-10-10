package io.gridgo.socket.impl;

import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.utils.helper.Assert;
import io.gridgo.utils.helper.Loggable;

public abstract class AbstractSocketFactory implements SocketFactory, Loggable {

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends Socket> T createSocket(SocketOptions options) {
		Assert.notNull(options, "socket options");
		Assert.notNull(options.getType(), "socket type");

		Socket socket = this.createCustomSocket(options.getType());

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

		if (socket != null) {
			socket.applyConfig(options.getConfig());
			return (T) socket;
		}

		throw new IllegalArgumentException("Socket type " + options.getType() + " is not supported");
	}

	protected Socket createCustomSocket(String type) {
		return null;
	}

	protected Socket createPullSocket() {
		return null;
	}

	protected Socket createPushSocket() {
		return null;
	}

	protected Socket createPubSocket() {
		return null;
	}

	protected Socket createSubSocket() {
		return null;
	}

	protected Socket createReqSocket() {
		return null;
	}

	protected Socket createRepSocket() {
		return null;
	}

	protected Socket createPairSocket() {
		return null;
	}

}
