package io.gridgo.socket.zmq.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import io.gridgo.bean.BObject;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.agent.impl.DefaultSocketSender;
import io.gridgo.socket.zmq.ZMQSocketFactory;
import io.gridgo.utils.ThreadUtils;

public class PushClient extends DefaultSocketSender {

	public static void main(String[] args) throws InterruptedException {
		final PushClient client = new PushClient();

		Runtime.getRuntime().addShutdownHook(new Thread(client::stop, "shutdown-thread"));
		client.start();

		int numMessages = (int) 1e6;
		int messageSize = 1024;

		BObject obj = BObject.newFromSequence("name", "Nguyễn Hoàng Bách", "age", 30, "total", numMessages);

		final ByteBuffer buffer = ByteBuffer.allocateDirect(messageSize);
		long start = System.nanoTime();
		for (int i = 0; i < numMessages; i++) {
			obj.putAny("index", i);
			buffer.clear();
			obj.writeBytes(buffer);
			buffer.rewind();
			client.send(buffer);
		}

		double elaspedSeconds = Double.valueOf(System.nanoTime() - start) / 1e9;

		System.out.printf("Total sent message: %,d\n", client.getTotalSentMsg());
		System.out.printf("Message rate: %,.2f (msg/s)\n",
				(Double.valueOf(client.getTotalSentMsg()).doubleValue() / elaspedSeconds));
		System.out.printf("Throughput: %,.2f (KB/s)\n",
				(Double.valueOf(client.getTotalSentBytes()).doubleValue() / 1024 / elaspedSeconds));

		final CountDownLatch exitSignal = new CountDownLatch(1);
		Thread keepAlive = new Thread(() -> {
			try {
				exitSignal.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			exitSignal.countDown();
		}));

		keepAlive.start();

		ThreadUtils.sleep(2000);

		System.exit(0);
	}

	PushClient() {
		this.setSocket(this.createSocket());
	}

	protected Socket createSocket() {
		SocketOptions options = new SocketOptions();
		options.setType("push");
		Socket socket = new ZMQSocketFactory().createSocket(options);
		String address = "tcp://localhost:8888";
		socket.connect(address);
		return socket;
	}
}
