package io.gridgo.socket;

import java.nio.ByteBuffer;

public interface BrokerlessSocket extends Socket<ByteBuffer>, Bindable, Connectable, HasEndpoint, Configurable {

	default int send(byte[] bytes) {
		return this.send(bytes, true);
	}

	default int send(byte[] bytes, boolean block) {
		return this.send(ByteBuffer.wrap(bytes).flip(), block);
	}

	default int receive(ByteBuffer buffer) {
		return this.receive(buffer, true);
	}

	int receive(ByteBuffer buffer, boolean block);
}
