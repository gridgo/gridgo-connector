package io.gridgo.socket.zmq.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.agent.impl.DefaultSocketReceiver;
import io.gridgo.socket.zmq.ZMQSocketFactory;

public class PullServer extends DefaultSocketReceiver {

	public static void main(String[] args) throws InterruptedException {

		final PullServer server = new PullServer();
		final CountDownLatch doneSignal = new CountDownLatch(1);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			server.stop();
			doneSignal.countDown();
		}, "shutdown-thread"));

		server.start();
		doneSignal.await();
	}

	long lastRecvBytes = 0;
	long lastRecvMessageCount = 0;
	private Thread monitorThread;

	public PullServer() {
		this.setSocket(createSocket());
		this.setBufferSize(2048);
		this.setConsumer(this::onReceive);
	}

	private final AtomicInteger recvIndex = new AtomicInteger(0);

	void onReceive(int length, ByteBuffer data) {
		BElement ele = BElement.fromRaw(data);
		if (ele instanceof BObject) {
			BObject obj = ele.asObject();
			int index = obj.getInteger("index");
			if (index == 0) {
				recvIndex.set(1);
			} else if (recvIndex.getAndIncrement() != index) {
				System.err.println("Expected index " + recvIndex.get() + ", got: " + index);
			}
		} else {
			System.err.println("Got data cannot be parsed to BObject: " + new String(ele.asValue().getRaw()));
		}
	}

	@Override
	protected void onStartSuccess() {
		lastRecvMessageCount = 0;
		this.monitorThread = new Thread(this::monitor);
		this.monitorThread.start();
	}

	private void monitor() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}

			long newRecvMessageCount = this.getTotalRecvMsg();
			long newRecvBytes = this.getTotalRecvBytes();
			if (newRecvBytes > lastRecvBytes) {
				long deltaRecvBytes = newRecvBytes - lastRecvBytes;
				long deltaRecvMsgCount = newRecvMessageCount - lastRecvMessageCount;

				lastRecvBytes = newRecvBytes;
				lastRecvMessageCount = newRecvMessageCount;

				System.out.printf("Message rate: %,d (msg/s) at throughput: %,.2f KB/s \n", deltaRecvMsgCount,
						Double.valueOf(deltaRecvBytes) / 1024);
			}
		}
	}

	@Override
	protected void onFinally() {
		if (this.monitorThread != null) {
			if (this.monitorThread.isAlive()) {
				this.monitorThread.interrupt();
			}
			this.monitorThread = null;
		}
	}

	private Socket createSocket() {
		SocketOptions options = new SocketOptions();
		options.setType("pull");
		options.addConfig("receiveTimeout", 100);

		Socket socket = new ZMQSocketFactory().createSocket(options);
		String address = "tcp://localhost:8888";
		socket.bind(address);
		return socket;
	}
}
