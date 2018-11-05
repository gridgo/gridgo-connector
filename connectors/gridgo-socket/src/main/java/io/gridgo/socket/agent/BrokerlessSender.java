package io.gridgo.socket.agent;

import java.nio.ByteBuffer;

public interface BrokerlessSender extends BrokerlessAgent {

	int send(ByteBuffer payload, boolean block);

	default int send(ByteBuffer payload) {
		return send(payload, true);
	}

	long getTotalSentBytes();

	long getTotalSentMsg();
}
