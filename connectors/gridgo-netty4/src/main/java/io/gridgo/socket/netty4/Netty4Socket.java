package io.gridgo.socket.netty4;

import java.io.Closeable;
import java.util.Map;
import java.util.Map.Entry;

import io.gridgo.framework.ComponentLifecycle;

public interface Netty4Socket extends Closeable, ComponentLifecycle {

	boolean isStarted();

	void applyConfig(String name, Object value);

	default void applyConfigs(Map<String, ?> configMap) {
		for (Entry<String, ?> entry : configMap.entrySet()) {
			this.applyConfig(entry.getKey(), entry.getValue());
		}
	}
}
