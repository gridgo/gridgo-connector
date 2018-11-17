package io.gridgo.socket.test.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.commons.io.IOUtils;

import io.gridgo.socket.Socket;
import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;
import lombok.Getter;

public class TestSocket implements Socket {

	private java.net.Socket socket;

	private java.net.ServerSocket serverSocket;

	@Getter
	private Endpoint endpoint;

	public TestSocket() {
	}

	@Override
	public boolean isAlive() {
		return socket != null && !socket.isClosed();
	}

	@Override
	public void close() {
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
	public int send(ByteBuffer message, boolean block) {
		if (socket == null)
			throw new IllegalStateException("socket is null");
		byte[] arr = new byte[1 + message.remaining()];
		arr[0] = (byte) (arr.length - 1);
		message.get(arr, 1, arr.length - 1);
		try {
			IOUtils.write(arr, socket.getOutputStream());
			socket.getOutputStream().flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return arr.length;
	}

	@Override
	public int receive(ByteBuffer buffer, boolean block) {
		try (var socket = serverSocket.accept()) {
			byte[] arr;
			byte[] length = IOUtils.toByteArray(socket.getInputStream(), 1);
			byte size = length[0];
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
	public void connect(String address) {
		this.endpoint = EndpointParser.parse(address);
		try {
			socket = new java.net.Socket();
			socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void bind(String address) {
		this.endpoint = EndpointParser.parse(address);
		try {
			serverSocket = new java.net.ServerSocket();
			serverSocket.setSoTimeout(100);
			serverSocket.bind(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void applyConfig(String name, Object value) {

	}
}
