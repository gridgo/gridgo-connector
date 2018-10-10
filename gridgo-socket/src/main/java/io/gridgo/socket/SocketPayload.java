package io.gridgo.socket;

import java.nio.ByteBuffer;

public interface SocketPayload {

	int getId();

	ByteBuffer getBuffer();
}
