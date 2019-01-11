package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConstants;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketUtils {

    public static Message accumulateBatch(@NonNull Collection<Message> messages) {
        if (messages.size() == 1) {
            return messages.iterator().next();
        }

        BArray body = BArray.ofEmpty();
        for (Message mess : messages) {
            Payload payload = mess.getPayload();
            body.add(BArray.ofSequence(payload.getId().orElse(null), payload.getHeaders(), payload.getBody()));
        }
        Payload payload = Payload.of(body)//
                                 .addHeader(SocketConstants.IS_BATCH, true) //
                                 .addHeader(SocketConstants.BATCH_SIZE, messages.size());

        return Message.of(payload);
    }

    private static void process(ByteBuffer buffer, boolean skipTopicHeader, Consumer<Message> receiver, Consumer<Integer> recvByteCounter,
            Consumer<Integer> recvMsgCounter, Consumer<Throwable> exceptionHandler, int rc) {
        recvByteCounter.accept(rc);

        try {
            buffer.flip();
            if (skipTopicHeader) {
                byte b = buffer.get();
                while (b != 0) {
                    b = buffer.get();
                }
            }

            var message = Message.parse(buffer);
            BObject headers = message.headers();
            if (headers != null && headers.getBoolean(SocketConstants.IS_BATCH, false)) {
                processBatch(receiver, recvMsgCounter, message, headers);
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

    private static void processBatch(Consumer<Message> receiver, Consumer<Integer> recvMsgCounter, Message message, BObject headers) {
        var subMessages = message.body().asArray();
        recvMsgCounter.accept(headers.getInteger(SocketConstants.BATCH_SIZE, subMessages.size()));
        for (BElement payload : subMessages) {
            var subMessage = Message.parse(payload);
            receiver.accept(subMessage);
        }
    }

    public static void startPolling( //
            Socket socket, //
            ByteBuffer buffer, //
            boolean skipTopicHeader, //
            Consumer<Message> receiver, //
            Consumer<Integer> recvByteCounter, //
            Consumer<Integer> recvMsgCounter, //
            Consumer<Throwable> exceptionHandler, //
            Consumer<CountDownLatch> doneSignalOutput) {

        var doneSignal = new CountDownLatch(1);

        if (doneSignalOutput != null) {
            doneSignalOutput.accept(doneSignal);
        }

        while (!Thread.currentThread().isInterrupted()) {
            buffer.clear();
            int rc = socket.receive(buffer);

            if (rc < 0) {
                if (Thread.currentThread().isInterrupted())
                    break;
            } else {
                process(buffer, skipTopicHeader, receiver, recvByteCounter, recvMsgCounter, exceptionHandler, rc);
            }
        }

        doneSignal.countDown();
    }
}
