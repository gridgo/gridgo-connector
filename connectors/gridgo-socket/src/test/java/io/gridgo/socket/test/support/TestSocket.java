package io.gridgo.socket.test.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.commons.io.IOUtils;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;

public class TestSocket extends AbstractSocket {

    private java.net.Socket socket;

    private java.net.ServerSocket serverSocket;

    public TestSocket() {
    }

    @Override
    public void applyConfig(String name, Object value) {

    }

    @Override
    protected void doBind(Endpoint endpoint) {
        try {
            serverSocket = new java.net.ServerSocket();
            serverSocket.setSoTimeout(100);
            serverSocket.bind(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doClose() {
        try {
            if (socket != null)
                socket.close();
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doConnect(Endpoint endpoint) {
        try {
            socket = new java.net.Socket();
            socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int doReveive(ByteBuffer buffer, boolean block) {
        try {
            if (socket == null)
                socket = serverSocket.accept();
            byte[] arr;
            byte[] length;
            try {
                length = IOUtils.toByteArray(socket.getInputStream(), 4);
            } catch (IOException ex) {
                return -1;
            }
            ByteBuffer bb = ByteBuffer.wrap(length);
            int size = bb.getInt();

            arr = IOUtils.toByteArray(socket.getInputStream(), size);
            buffer.put(arr);
            return arr.length;
        } catch (SocketTimeoutException e) {
            return -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    @Override
    protected int doSend(ByteBuffer buffer, boolean block) {
        if (socket == null)
            throw new IllegalStateException("socket is null");
        byte[] arr = new byte[4 + buffer.limit()];
        ByteBuffer bb = ByteBuffer.wrap(arr);
        bb.putInt(arr.length - 4);
        buffer.get(arr, 4, arr.length - 4);
        try {
            IOUtils.write(arr, socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return arr.length;
    }

    @Override
    public int doSubscribe(String topic) {
        throw new UnsupportedOperationException("Cannot subscribe on raw socket");
    }
}
