package io.gridgo.socket.agent;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public interface BrokerlessReceiver extends BrokerlessAgent {

	void setBufferSize(int bufferSize);

	void setConsumer(BiConsumer<Integer, ByteBuffer> consumer);

	long getTotalRecvBytes();

	long getTotalRecvMsg();
}
