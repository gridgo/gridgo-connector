package io.gridgo.socket.netty4.ws;

public enum Netty4WebsocketFrameType {

    TEXT, BINARRY;

    public static final Netty4WebsocketFrameType fromName(String name) {
        if (name != null) {
            name = name.trim();
            for (Netty4WebsocketFrameType value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static final Netty4WebsocketFrameType fromNameOrDefault(String name, Netty4WebsocketFrameType defaultValue) {
        Netty4WebsocketFrameType result = fromName(name);
        return result == null ? defaultValue : result;
    }
}
