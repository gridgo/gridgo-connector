package io.gridgo.socket.agent;

import java.nio.ByteBuffer;

import io.gridgo.socket.SocketPayload;
import io.gridgo.socket.agent.impl.DefaultSocketPayload;

public interface SocketSender extends SocketAgent {

	int send(SocketPayload payload, boolean block);

	default int send(SocketPayload payload) {
		return send(payload, true);
	}

	default int send(ByteBuffer buffer) {
		return this.send(new DefaultSocketPayload(buffer));
	}

	default int send(byte[] bytes) {
		return this.send(ByteBuffer.wrap(bytes));
	}

	long getTotalSentBytes();

	long getTotalSentMsg();
}
