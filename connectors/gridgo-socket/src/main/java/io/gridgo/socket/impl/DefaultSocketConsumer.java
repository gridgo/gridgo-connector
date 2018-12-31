package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.SocketConsumer;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.utils.ThreadUtils;
import lombok.Getter;

public class DefaultSocketConsumer extends AbstractHasResponderConsumer implements SocketConsumer {

    @Getter
    private long totalRecvBytes;

    @Getter
    private long totalRecvMessages;

    private Thread poller;

    private final int bufferSize;

    private final SocketFactory factory;

    private final SocketOptions options;

    private final String address;

    private CountDownLatch stopDoneTrigger;

    private boolean autoSkipTopicHeader = false;

    public DefaultSocketConsumer(ConnectorContext context, SocketFactory factory, SocketOptions options, String address, int bufferSize) {
        super(context);
        this.factory = factory;
        this.options = options;
        this.address = address;
        this.bufferSize = bufferSize;
    }

    @Override
    protected String generateName() {
        return "consumer." + this.getUniqueIdentifier();
    }

    private String getUniqueIdentifier() {
        return new StringBuilder() //
                                  .append(this.factory.getType()) //
                                  .append(".") //
                                  .append(this.options.getType()) //
                                  .append(".") //
                                  .append(this.address) //
                                  .toString();
    }

    private Socket initSocket() {
        Socket socket = this.factory.createSocket(options);
        if (!options.getConfig().containsKey("receiveTimeout")) {
            socket.applyConfig("receiveTimeout", DEFAULT_RECV_TIMEOUT);
        }

        switch (options.getType().toLowerCase()) {
        case "pull":
            socket.bind(address);
            break;
        case "sub":
            socket.connect(address);
            String topic = (String) options.getConfig().getOrDefault("topic", "");
            socket.subscribe(topic);
            this.autoSkipTopicHeader = true;
            break;
        case "pair":
            socket.bind(address);
            int maxBatchSize = 0;
            boolean batchingEnabled = Boolean.parseBoolean((String) this.options.getConfig().get("batchingEnabled"));
            if (batchingEnabled) {
                maxBatchSize = Integer.valueOf((String) this.options.getConfig().getOrDefault("maxBatchingSize", SocketConnector.DEFAULT_MAX_BATCH_SIZE));
            }
            this.setResponder(new DefaultSocketResponder(getContext(), socket, bufferSize, 1024, batchingEnabled, maxBatchSize, this.getUniqueIdentifier()));
            break;
        default:
        }
        return socket;
    }

    @Override
    protected void onStart() {

        final Socket socket = initSocket();

        final AtomicReference<CountDownLatch> doneSignalRef = new AtomicReference<CountDownLatch>();
        this.poller = new Thread(() -> {
            this.poll(socket, (doneSignal) -> {
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
    protected final void onStop() {
        if (this.poller != null && !this.poller.isInterrupted()) {
            this.poller.interrupt();
            this.poller = null;
            try {
                this.stopDoneTrigger.await();
                this.stopDoneTrigger = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("error while waiting for stopped", e);
            }
        }
    }

    private void poll(Socket socket, Consumer<CountDownLatch> stopDoneTriggerOutput) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferSize);
        Thread.currentThread().setName("[POLLER] " + this.getName());
        SocketUtils.startPolling(socket, buffer, this.autoSkipTopicHeader, (message) -> {
            ensurePayloadId(message);
            publish(message, null);
        }, (recvBytes) -> {
            totalRecvBytes += recvBytes;
        }, (recvMsgs) -> {
            totalRecvMessages += recvMsgs;
        }, this.getContext().getExceptionHandler(), stopDoneTriggerOutput);

        socket.close();
        this.poller = null;
    }
}
