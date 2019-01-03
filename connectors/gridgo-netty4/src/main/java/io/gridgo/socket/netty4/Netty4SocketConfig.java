package io.gridgo.socket.netty4;

import io.gridgo.bean.BObject;
import io.gridgo.utils.support.HostAndPort;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Netty4SocketConfig {

    private Netty4Transport transport;
    private HostAndPort host;
    private String path;

    private BObject options;
}
