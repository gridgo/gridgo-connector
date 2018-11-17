package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketPollingUtils {

	public static void startPolling( //
			Socket socket, //
			ByteBuffer buffer, //
			Consumer<Message> receiver, //
			Consumer<Integer> recvByteCounter, //
			Consumer<Integer> recvMsgCounter, //
			Consumer<Throwable> exceptionHandler, //
			Consumer<CountDownLatch> doneSignalOutput) {

		final CountDownLatch doneSignal = new CountDownLatch(1);

		if (doneSignalOutput != null) {
			doneSignalOutput.accept(doneSignal);
		}

		while (!Thread.currentThread().isInterrupted()) {
			buffer.clear();
			int rc = socket.receive(buffer);

			if (rc < 0) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
			} else {
				recvByteCounter.accept(rc);

				Message message = null;
				try {
					message = Message.parse(buffer.flip());
					BObject headers = message.getPayload().getHeaders();
					if (headers != null && headers.getBoolean(SocketConstants.IS_BATCH, false)) {
						BArray subMessages = message.getPayload().getBody().asArray();
						recvMsgCounter.accept(headers.getInteger(SocketConstants.BATCH_SIZE, subMessages.size()));
						for (BElement payload : subMessages) {
							Message subMessage = Message.parse(payload);
							receiver.accept(subMessage);
						}
					} else {
						recvMsgCounter.accept(1);
						receiver.accept(message);
					}
				} catch (Exception e) {
					if (exceptionHandler != null) {
						exceptionHandler.accept(e);
					} else {
						log.error("Error while parse buffer to message", e);
					}
				}
			}
		}

		doneSignal.countDown();
	}
}
