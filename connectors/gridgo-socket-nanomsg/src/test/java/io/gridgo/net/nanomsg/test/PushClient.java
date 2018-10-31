package io.gridgo.net.nanomsg.test;

import java.nio.ByteBuffer;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.agent.impl.DefaultSocketSender;
import io.gridgo.socket.nanomsg.NNSocket;
import io.gridgo.socket.nanomsg.NNSocketFactory;

public class PushClient extends DefaultSocketSender {

	public static void main(String[] args) throws InterruptedException {
		final PushClient client = new PushClient();

		Runtime.getRuntime().addShutdownHook(new Thread(client::stop, "shutdown-thread"));
		client.start();

		int numMessages = (int) 1e6;
		int messageSize = 1024;

		final ByteBuffer buffer = ByteBuffer.allocateDirect(messageSize);
		byte bval = 111;
		for (int i = 0; i < messageSize; ++i) {
			buffer.put(i, bval);
		}

		long start = System.nanoTime();
		for (int i = 0; i < numMessages; i++) {
			buffer.rewind();
			client.send(buffer);
		}

		double elaspedSeconds = Double.valueOf(System.nanoTime() - start) / 1e9;

		System.out.printf("Total sent message: %,d\n", client.getTotalSentMsg());
		System.out.printf("Message rate: %,.2f (msg/s)\n",
				(Double.valueOf(client.getTotalSentMsg()).doubleValue() / elaspedSeconds));
		System.out.printf("Throughput: %,.2f (KB/s)\n",
				(Double.valueOf(client.getTotalSentBytes()).doubleValue() / 1024 / elaspedSeconds));

		System.exit(0);
	}

	PushClient() {
		this.setSocket(this.createSocket());
	}

	protected BrokerlessSocket createSocket() {
		SocketOptions options = new SocketOptions();
		options.setType("push");
		NNSocket socket = new NNSocketFactory().createSocket(options);
		String address = "tcp://localhost:8888";
		socket.connect(address);
		return socket;
	}
}
