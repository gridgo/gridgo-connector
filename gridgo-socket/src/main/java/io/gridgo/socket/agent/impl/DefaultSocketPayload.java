package io.gridgo.socket.agent.impl;

import java.nio.ByteBuffer;

import io.gridgo.socket.SocketPayload;
import lombok.Data;

@Data
public class DefaultSocketPayload implements SocketPayload {

	private int id;

	private ByteBuffer buffer;

	public DefaultSocketPayload() {

	}

	public DefaultSocketPayload(int id) {
		this.id = id;
	}

	public DefaultSocketPayload(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public DefaultSocketPayload(ByteBuffer buffer, int id) {
		this(buffer);
		this.id = id;
	}

}
