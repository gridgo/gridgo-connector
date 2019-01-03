package io.gridgo.socket.netty4;

import java.io.Closeable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.utils.helper.Loggable;

public interface Netty4Socket extends Closeable, ComponentLifecycle, Loggable {

    void applyConfig(String name, Object value);

    default void applyConfigs(Map<String, ?> configMap) {
        for (Entry<String, ?> entry : configMap.entrySet()) {
            this.applyConfig(entry.getKey(), entry.getValue());
        }
    }

    @Override
    default String getName() {
        getLogger().info("Netty4 Socket is nameless component, so, getName() is unsupported");
        return null;
    }

    boolean isRunning();

    boolean isStarted();

    void setFailureHandler(Consumer<Throwable> failureHandler);
}
