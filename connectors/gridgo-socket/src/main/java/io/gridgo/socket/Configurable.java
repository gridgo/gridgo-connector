package io.gridgo.socket;

import java.util.Map;
import java.util.Map.Entry;

import io.gridgo.utils.helper.Assert;

public interface Configurable {

    default void applyConfig(Map<String, Object> options) {
        Assert.notNull(options, "Options");
        for (Entry<String, Object> entry : options.entrySet()) {
            this.applyConfig(entry.getKey(), entry.getValue());
        }
    }

    void applyConfig(String name, Object value);
}
