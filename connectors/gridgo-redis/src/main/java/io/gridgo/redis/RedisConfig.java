package io.gridgo.redis;

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

    private String sentinelMasterId;

    private Function<Object, BElement> parser;
}
