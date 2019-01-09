package io.gridgo.socket.netty4.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.socket.netty4.Netty4Socket;
import io.gridgo.socket.netty4.Netty4Transport;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.helper.Loggable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractNetty4Socket implements Netty4Socket, Loggable {

    @Getter(AccessLevel.PROTECTED)
    private final BObject configs = BObject.ofEmpty();

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Netty4Transport transport = null;

    private final AtomicBoolean startFlag = new AtomicBoolean(false);

    @Getter
    private boolean running = false;

    @Setter
    private Consumer<Throwable> failureHandler;

    @Override
    public final void applyConfig(@NonNull String name, @NonNull Object value) {
        this.configs.putAny(name, value);
        this.onApplyConfig(name);
    }

    protected void onApplyConfig(String name) {
        // do nothing
    }

    @Override
    public final void close() throws IOException {
        if (this.isStarted() && this.startFlag.compareAndSet(true, false)) {
            try {
                this.onClose();
            } catch (ClosedChannelException e) {
                // channel already closed, just ignore
            }
            this.running = false;
        }
    }

    protected abstract BElement handleIncomingMessage(String channelId, Object msg) throws Exception;

    protected final boolean isInChangingState() {
        return startFlag.get() != running;
    }

    protected final boolean isOkToClose() {
        return this.startFlag.get() && this.running;
    }

    @Override
    public final boolean isStarted() {
        ThreadUtils.busySpin(10, this::isInChangingState);
        return this.running;
    }

    protected ChannelInboundHandler newChannelHandlerDelegater() {
        return new SimpleChannelInboundHandler<Object>() {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                AbstractNetty4Socket.this.onChannelActive(ctx);
                ctx.fireChannelActive();
            };

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                AbstractNetty4Socket.this.onChannelInactive(ctx);
                ctx.fireChannelInactive();
            }

            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                AbstractNetty4Socket.this.onChannelRead(ctx, msg);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                AbstractNetty4Socket.this.onException(ctx, cause);
            }

            @Override
            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                AbstractNetty4Socket.this.onHandlerAdded(ctx);
            }
        };
    }

    protected abstract void onChannelActive(ChannelHandlerContext ctx) throws Exception;

    protected abstract void onChannelInactive(ChannelHandlerContext ctx) throws Exception;

    protected abstract void onChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    protected void onClose() throws IOException {

    }

    protected void onException(ChannelHandlerContext ctx, Throwable cause) {
        if (this.failureHandler != null) {
            this.failureHandler.accept(cause);
        }
        getLogger().error("Error while handling socket msg", cause);
    }

    protected void onHandlerAdded(ChannelHandlerContext ctx) {
        // do nothing...
    }

    @Override
    public void stop() {
        try {
            this.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while close netty4 socket", e);
        }
    }

    protected final boolean tryStart(Runnable starter) {
        if (!this.isStarted() && this.startFlag.compareAndSet(false, true)) {
            try {
                starter.run();
            } catch (Exception e) {
                this.startFlag.set(false);
                throw e;
            }
            this.running = true;
            return true;
        }
        return false;
    }
}
