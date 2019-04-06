package io.gridgo.socket.netty4.utils;

import lombok.NonNull;

public enum KeyStoreType {

    JCEKS, JKS, DKS, PKCS11, PKCS12;

    public static final KeyStoreType forName(@NonNull String name) {
        for (KeyStoreType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
