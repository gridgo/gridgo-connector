package io.gridgo.socket;

public interface SocketFactory {

    <T extends Socket> T createSocket(SocketOptions options);

    String getType();
}
