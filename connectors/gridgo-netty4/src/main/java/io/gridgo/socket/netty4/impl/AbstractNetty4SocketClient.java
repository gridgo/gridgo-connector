package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.Netty4SocketClient;
import io.gridgo.socket.netty4.Netty4SocketOptionsUtils;
import io.gridgo.utils.support.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4SocketClient extends AbstractNetty4Socket implements Netty4SocketClient {

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private Consumer<BElement> receiveCallback;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private Runnable channelOpenCallback;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private Runnable channelCloseCallback;

    @Getter(AccessLevel.PROTECTED)
    private Channel channel;

    private ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            AbstractNetty4SocketClient.this.initChannel(ch);
        }
    };

    private Bootstrap bootstrap;

    private ChannelFuture connectFuture;

    @Override
    public void connect(@NonNull final HostAndPort host) {
        tryStart(() -> {
            final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
            final CountDownLatch doneSignal = new CountDownLatch(1);
            final Deferred<Void, Throwable> deferred = new AsyncDeferredObject<>();

            deferred.promise().always((stt, result, failedCause) -> {
                if (stt == DeferredStatus.REJECTED) {
                    if (failedCause == null) {
                        failedCause = new RuntimeException("Unknown error, cannot connect to server");
                    }
                    exceptionRef.set(failedCause);
                }
                doneSignal.countDown();
            });

            new Thread(() -> {
                this.executeConnect(host, deferred);
            }).start();

            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            if (exceptionRef.get() != null) {
                throw new RuntimeException(exceptionRef.get());
            }
        });
    }

    protected Bootstrap createBootstrap() {
        return new Bootstrap().channel(NioSocketChannel.class);
    }

    protected NioEventLoopGroup createLoopGroup() {
        return new NioEventLoopGroup(this.getConfigs().getInteger("workerThreads", 1));
    }

    private void executeConnect(HostAndPort host, Deferred<Void, Throwable> deferred) {
        try {
            this.onBeforeConnect(host);
        } catch (Exception e) {
            deferred.reject(e);
            return;
        }

        final NioEventLoopGroup loopGroup = createLoopGroup();

        bootstrap = createBootstrap();
        bootstrap.group(loopGroup);
        bootstrap.handler(this.channelInitializer);

        Netty4SocketOptionsUtils.applyOptions(getConfigs(), bootstrap);

        this.connectFuture = bootstrap.connect(host.getHostOrDefault("localhost"), host.getPort());

        boolean success = false;
        try {
            success = connectFuture.await().isSuccess();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            deferred.reject(new RuntimeException("Error while connect to " + host, e));
            return;
        }

        try {
            if (!success) {
                deferred.reject(connectFuture.cause());
            } else {
                this.channel = connectFuture.channel();
                try {
                    this.onAfterConnect();
                } catch (Exception e) {
                    this.channel.close();
                    this.channel = null;
                    deferred.reject(e);
                    return;
                }
                deferred.resolve(null);

                getLogger().info("Connect success to {}", host.toIpAndPort());
                this.connectFuture.channel().closeFuture().await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            getLogger().error("Error while waiting for connectFuture tobe closed", e);
        } finally {
            loopGroup.shutdownGracefully();
        }
    }

    protected abstract BElement handleIncomingMessage(Object msg) throws Exception;

    @Override
    protected final BElement handleIncomingMessage(String channelId, Object msg) throws Exception {
        return this.handleIncomingMessage(msg);
    }

    private void initChannel(SocketChannel channel) {
        this.onInitChannel(channel);
        channel.pipeline().addLast("handler", this.newChannelHandlerDelegater());
    }

    protected void onAfterConnect() {
        // do nothing
    }

    @Override
    protected void onApplyConfig(String name) {
        if (this.isStarted()) {
            Netty4SocketOptionsUtils.applyOption(name, getConfigs(), bootstrap);
        }
    }

    protected void onBeforeConnect(HostAndPort host) {
        // do nothing
    }

    @Override
    protected void onChannelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.getChannelOpenCallback() != null) {
            this.getChannelOpenCallback().run();
        }
    }

    @Override
    protected final void onChannelInactive(ChannelHandlerContext ctx) throws Exception {
//		System.out.println(this.getClass().getSimpleName() + " --> channel inactive");
        if (isOkToClose()) {
            // make sure the client state is sync with channel state, call close, the
            // closedChannelException were ignored

//			System.out.println(this.getClass().getSimpleName()
//					+ " --> channel inactive by connection itself, calling close to sync the state");
            this.close();
        }

        if (this.getChannelCloseCallback() != null) {
//			System.out.println(this.getClass().getSimpleName() + " --> calling channel close callback...");
            this.getChannelCloseCallback().run();
        }
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (this.getReceiveCallback() != null) {
            this.getReceiveCallback().accept(this.handleIncomingMessage("", msg));
        }
    }

    @Override
    protected void onClose() throws IOException {
        if (this.getChannel().isOpen()) {
            this.getChannel().close().syncUninterruptibly();
        }
    }

    protected abstract void onInitChannel(SocketChannel ch);

    @Override
    public ChannelFuture send(BElement data) {
        return this.channel.writeAndFlush(data);
    }
}
