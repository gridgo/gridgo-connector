package io.gridgo.socket;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.utils.helper.Assert;

public interface Socket {

	boolean isAlive();

	void close();

	int send(ByteBuffer message, boolean block);

	default int send(ByteBuffer message) {
		return this.send(message, true);
	}

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

	void connect(String address);

	void bind(String address);

	void subscribe(String topic);

	Endpoint getEndpoint();

	void applyConfig(String name, Object value);

	default void applyConfig(Map<String, Object> options) {
		Assert.notNull(options, "Options");
		for (Entry<String, Object> entry : options.entrySet()) {
			this.applyConfig(entry.getKey(), entry.getValue());
		}
	}
}
