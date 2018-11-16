package io.gridgo.socket;

public interface SocketFactory {

	String getType();

	<T extends Socket> T createSocket(SocketOptions options);
}
