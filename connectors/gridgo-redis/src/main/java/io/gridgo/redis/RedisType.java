package io.gridgo.redis;

import java.util.HashSet;
import java.util.Set;

public enum RedisType {

    SINGLE, MASTER_SLAVE("masterSlave"), SENTINEL, CLUSTER;

    public static RedisType forName(String name) {
        if (name != null) {
            name = name.trim().toLowerCase();
            for (RedisType type : values()) {
                if (type.aliases.contains(name)) {
                    return type;
                }
            }
        }
        return null;
    }

    private Set<String> aliases = new HashSet<>();

    private RedisType() {
        this.aliases.add(this.name().toLowerCase());
    }

    private RedisType(String... aliases) {
        this();
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                this.aliases.add(alias.trim().toLowerCase());
            }
        }
    }
}
