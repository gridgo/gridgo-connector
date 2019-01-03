package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.AsyncDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4SocketOptionsUtils;
import io.gridgo.socket.netty4.Netty4SocketServer;
import io.gridgo.utils.support.HostAndPort;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4SocketServer extends AbstractNetty4Socket implements Netty4SocketServer {

    private static final AttributeKey<Map<String, Object>> CHANNEL_DETAILS = AttributeKey.newInstance("channelDetails");

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private BiConsumer<String, BElement> receiveCallback;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private Consumer<String> channelOpenCallback;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private Consumer<String> channelCloseCallback;

    private final Map<String, Channel> channels = new NonBlockingHashMap<>();

    private final ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {

        @Override
        public void initChannel(SocketChannel socketChannel) throws Exception {
            AbstractNetty4SocketServer.this.initChannel(socketChannel);
        }
    };

    private Channel serverChannel;

    private ServerBootstrap bootstrap;

    @Override
    public void bind(@NonNull final HostAndPort host) {
        tryStart(() -> {
            final CountDownLatch doneSignal = new CountDownLatch(1);
            final AtomicReference<Throwable> failedCauseRef = new AtomicReference<>();

            Deferred<Void, Throwable> deferred = new AsyncDeferredObject<>();
            deferred.promise().always((stt, msg, failedCause) -> {
                if (stt == DeferredStatus.REJECTED) {
                    failedCauseRef.set(failedCause == null ? new Exception("Unknown exception") : failedCause);
                }
                doneSignal.countDown();
            });

            new Thread(() -> {
                executeBind(host, deferred);
            }).start();

            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            if (failedCauseRef.get() != null) {
                throw new RuntimeException(failedCauseRef.get());
            }
        });
    }

    protected void closeChannel(Channel channel) {
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected ServerBootstrap createBootstrap() {
        return new ServerBootstrap().channel(NioServerSocketChannel.class);
    }

    private void executeBind(HostAndPort host, Deferred<Void, Throwable> deferred) {

        try {
            this.onBeforeBind(host);
        } catch (Exception e) {
            deferred.reject(e);
            return;
        }

        BObject configs = this.getConfigs();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(configs.getInteger("bossThreads", 1));
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(configs.getInteger("workerThreads", 1));

        bootstrap = createBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.childHandler(this.channelInitializer);

        Netty4SocketOptionsUtils.applyOptions(getConfigs(), bootstrap);

        // Bind and start to accept incoming connections.
        final ChannelFuture bindFuture = bootstrap.bind(host.getResolvedIpOrDefault("127.0.0.1"), host.getPort());

        try {
            if (!bindFuture.await().isSuccess()) {
                deferred.reject(bindFuture.cause());
            } else {
                getLogger().info("Bind success to {}", host.toIpAndPort());
                // this.setHost(host);
                this.serverChannel = bindFuture.channel();

                try {
                    this.onAfterBind();
                } catch (Exception e) {
                    this.serverChannel.close().sync();
                    this.serverChannel = null;
                    deferred.reject(e);
                    return;
                }

                deferred.resolve(null);
                // block thread here and wait for server to shutdown
                bindFuture.channel().closeFuture().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            deferred.reject(e);
        } finally {
            // shutdown the nio event loop groups
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    protected String extractChannelId(Channel channel) {
        if (channel != null) {
            return channel.id().asLongText();
        }
        return null;
    }

    protected Channel getChannel(String id) {
        return this.channels.get(id);
    }

    @Override
    public Map<String, Object> getChannelDetails(String channelId) {
        Channel channel = this.getChannel(channelId);
        if (channel != null) {
            return channel.attr(CHANNEL_DETAILS).get();
        }
        return null;
    }

    private void initChannel(SocketChannel socketChannel) {
        this.onInitChannel(socketChannel);
        socketChannel.pipeline().addLast(this.newChannelHandlerDelegater());
    }

    protected void onAfterBind() {
        // do nothing.
    }

    @Override
    protected void onApplyConfig(String name) {
        if (this.isStarted()) {
            Netty4SocketOptionsUtils.applyOption(name, getConfigs(), bootstrap);
        }
    }

    protected void onBeforeBind(HostAndPort host) {
        // do nothing.
    }

    @Override
    protected final void onChannelActive(final ChannelHandlerContext ctx) throws Exception {
        final var channel = ctx.channel();

        var channelId = extractChannelId(channel);
        this.channels.put(channelId, channel);

        Map<String, Object> channelDetails = new NonBlockingHashMap<>();
        channel.attr(CHANNEL_DETAILS).set(channelDetails);

        channelDetails.put("remoteAddress", channel.remoteAddress());
        channelDetails.put("localAddress", channel.localAddress());
        channelDetails.put("config", channel.config());
        channelDetails.put("metadata", channel.metadata());

        if (this.getChannelOpenCallback() != null) {
            this.getChannelOpenCallback().accept(channelId);
        }
    }

    @Override
    protected final void onChannelInactive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = ctx.channel();
        String id = extractChannelId(channel);
        if (id != null && this.channels.containsKey(id)) {
            if (channel == this.channels.get(id)) {
                this.channels.remove(id);
                if (this.getChannelCloseCallback() != null) {
                    this.getChannelCloseCallback().accept(id);
                }
            } else {
                throw new IllegalStateException("Something were wrong, the current inactive channel has registered with other channel context");
            }
        } else {
            getLogger().warn("The current inactive channel hasn't been registered");
        }
    }

    @Override
    protected final void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel channel = ctx.channel();
        String id = this.extractChannelId(channel);
        if (id != null) {
            if (this.getReceiveCallback() != null) {
                BElement incomingMessage = handleIncomingMessage(id, msg);
                if (incomingMessage != null) {
                    this.getReceiveCallback().accept(id, incomingMessage);
                }
            }
        }
    }

    @Override
    protected void onClose() throws IOException {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }

        this.channels.clear();

        try {
            this.serverChannel.close().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            getLogger().warn("Close netty4 socket server error", this.serverChannel);
        } finally {
            this.serverChannel = null;
        }
    }

    protected abstract void onInitChannel(SocketChannel socketChannel);
}
