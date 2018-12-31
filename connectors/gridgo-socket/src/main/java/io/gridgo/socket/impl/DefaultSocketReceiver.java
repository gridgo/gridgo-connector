package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.Socket;
import io.gridgo.utils.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;

public class DefaultSocketReceiver extends AbstractConsumer implements Receiver, FailureHandlerAware<DefaultSocketReceiver> {

    @Getter
    private long totalRecvBytes;

    @Getter
    private long totalRecvMessages;

    @Getter(AccessLevel.PROTECTED)
    private Function<Throwable, Message> failureHandler;

    private final String uniqueIdentifier;

    private Thread poller;

    private final Socket socket;

    private final int bufferSize;

    private CountDownLatch stopDoneTrigger;

    public DefaultSocketReceiver(ConnectorContext context, Socket socket, int bufferSize, String uniqueIdentifier) {
        super(context);
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.uniqueIdentifier = uniqueIdentifier;
        this.setFailureHandler(context.getExceptionHandler());
    }

    @Override
    protected String generateName() {
        return "receiver." + this.uniqueIdentifier;
    }

    @Override
    protected void onStart() {
        final AtomicReference<CountDownLatch> doneSignalRef = new AtomicReference<CountDownLatch>();

        this.poller = new Thread(() -> {
            this.poll(this.socket, (doneSignal) -> {
                doneSignalRef.set(doneSignal);
            });
        });

        this.totalRecvBytes = 0;
        this.totalRecvMessages = 0;

        this.poller.start();

        ThreadUtils.sleep(100);

        ThreadUtils.busySpin(10, () -> {
            return doneSignalRef.get() == null;
        });

        this.stopDoneTrigger = doneSignalRef.get();
    }

    @Override
    protected void onStop() {
        this.poller.interrupt();
        this.poller = null;
        try {
            this.stopDoneTrigger.await();
            this.stopDoneTrigger = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Await for stopped error", e);
        }
    }

    private void poll(Socket socket, Consumer<CountDownLatch> doneSignalOutput) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferSize);
        Thread.currentThread().setName("[POLLER] " + socket.getEndpoint().getAddress());

        SocketUtils.startPolling(socket, buffer, false, (message) -> {
            ensurePayloadId(message);
            publish(message, null);
        }, (recvBytes) -> {
            totalRecvBytes += recvBytes;
        }, (recvMsgs) -> {
            totalRecvMessages += recvMsgs;
        }, this.getContext().getExceptionHandler(), doneSignalOutput);

        socket.close();
        this.poller = null;
    }

    @Override
    public DefaultSocketReceiver setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

}
