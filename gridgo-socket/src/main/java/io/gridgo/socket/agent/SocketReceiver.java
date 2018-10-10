package io.gridgo.socket.agent;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public interface SocketReceiver extends SocketAgent {

	void setConsumer(BiConsumer<Integer, ByteBuffer> consumer);

	long getTotalRecvBytes();

	long getTotalRecvMsg();
}
