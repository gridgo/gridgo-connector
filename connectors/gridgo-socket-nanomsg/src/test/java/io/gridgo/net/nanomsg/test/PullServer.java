package io.gridgo.net.nanomsg.test;

import java.util.concurrent.CountDownLatch;

import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.agent.impl.DefaultSocketReceiver;
import io.gridgo.socket.nanomsg.NNSocket;
import io.gridgo.socket.nanomsg.NNSocketFactory;

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
		options.addConfig("recvTimeout", 100);

		NNSocket socket = new NNSocketFactory().createSocket(options);
		String address = "tcp://127.0.0.1:8888";
		socket.bind(address);
		return socket;
	}
}
