package io.gridgo.test.rabbitmq;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.utils.ThreadUtils;

public class TestRabbitMQ {

	private static final String QUEUE_NAME = "test";
	private static AtomicLong recvMsgCount = new AtomicLong(0);
	private static AtomicLong recvBytesCount = new AtomicLong(0);

	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setHost("localhost");

		initConsumer(connFactory);

		new Thread(TestRabbitMQ::monitor).start();

		startTesting(connFactory);
	}

	private static void startTesting(ConnectionFactory connFactory) throws IOException, TimeoutException {
		Connection conn = connFactory.newConnection();
		Channel channel = conn.createChannel();

		BObject obj = BObject.newDefault();
		obj.putAny("name", "Nguyen Hoang Bach");
		obj.putAny("languages", new Object[] { "java", "js", "as3" });
		obj.putAny("age", 30);

		byte[] bytes = obj.toBytes();

		int numMessages = (int) 1e8;
		for (int i = 0; i < numMessages; i++) {
			try {
				channel.basicPublish("", QUEUE_NAME, null, bytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ThreadUtils.sleep(2000);
		System.out.println("****** DONE ******");
		System.exit(0);
	}

	private static void initConsumer(ConnectionFactory connFactory) throws IOException, TimeoutException {
		Connection conn = connFactory.newConnection();
		Channel channel = conn.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicConsume(QUEUE_NAME, false, TestRabbitMQ.createOnMessageCallback(channel), TestRabbitMQ::onCancel);
	}

	private static void monitor() {
		final DecimalFormat df = new DecimalFormat("###,###.##");
		long lastRecvMsgCount = 0, lastRecvBytesCount = 0;

		while (!Thread.currentThread().isInterrupted()) {
			ThreadUtils.sleep(1000);
			if (recvMsgCount.get() > lastRecvMsgCount) {
				long deltaRecvMsgCount = recvMsgCount.get() - lastRecvMsgCount;
				long deltaRecvBytesCount = recvBytesCount.get() - lastRecvBytesCount;

				lastRecvMsgCount += deltaRecvMsgCount;
				lastRecvBytesCount += deltaRecvBytesCount;

				System.out.println("Message rate: " + df.format(deltaRecvMsgCount) + " msg/s at throughput "
						+ df.format(Double.valueOf(deltaRecvBytesCount) / 1024 / 1024) + " MB/s");
			}
		}
	}

	private static DeliverCallback createOnMessageCallback(final Channel channel) {
		return (consumerTag, message) -> {
			if (message == null) {
				return;
			}
			System.out.println("Receiving Thread: " + Thread.currentThread().getName());
			recvMsgCount.incrementAndGet();
			byte[] body = message.getBody();
			recvBytesCount.addAndGet(body.length);
			BElement.fromRaw(body);
			channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
		};
	}

	private static void onCancel(String consumerTag) {
		System.out.println("Cancelled... " + consumerTag);
	}
}
