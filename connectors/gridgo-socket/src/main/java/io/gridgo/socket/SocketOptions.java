package io.gridgo.socket;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class SocketOptions {

    private String type;

    private final Map<String, Object> config = new HashMap<>();

    public SocketOptions addConfig(String name, Object value) {
        this.config.put(name, value);
        return this;
    }
}
