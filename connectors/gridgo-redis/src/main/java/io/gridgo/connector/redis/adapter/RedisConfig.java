package io.gridgo.connector.redis.adapter;

import java.util.function.Function;

import io.gridgo.bean.BElement;
import io.gridgo.utils.support.HostAndPortSet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RedisConfig {

    private HostAndPortSet address;

    private String password;

    private int database;

    private HostAndPortSet sentinelAddress;

    private Function<Object, BElement> parser;
}
