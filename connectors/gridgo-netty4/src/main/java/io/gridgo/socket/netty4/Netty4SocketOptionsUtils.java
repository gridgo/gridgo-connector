package io.gridgo.socket.netty4;

import io.gridgo.bean.BObject;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import lombok.NonNull;

public class Netty4SocketOptionsUtils {

    public static void applyOption( //
            @NonNull String name, //
            @NonNull BObject options, //
            @NonNull AbstractBootstrap<?, ?> bootstrap) {

        if (options.containsKey(name)) {
            switch (name.toLowerCase()) {
            case "solinger":
                bootstrap.option(ChannelOption.SO_LINGER, options.getInteger("soLinger"));
                break;
            case "sobacklog":
                bootstrap.option(ChannelOption.SO_BACKLOG, options.getInteger("soBacklog"));
                break;
            case "sokeepalive":
                bootstrap.option(ChannelOption.SO_KEEPALIVE, options.getBoolean("soKeepalive"));
                break;
            case "sobroadcast":
                bootstrap.option(ChannelOption.SO_BROADCAST, options.getBoolean("soBroadcast"));
                break;
            case "sorcvbuf":
                bootstrap.option(ChannelOption.SO_RCVBUF, options.getInteger("soRcvbuf"));
                break;
            case "sosndbuf":
                bootstrap.option(ChannelOption.SO_SNDBUF, options.getInteger("soSndbuf"));
                break;
            case "soreuseaddr":
                bootstrap.option(ChannelOption.SO_REUSEADDR, options.getBoolean("soReuseaddr"));
                break;
            case "sotimeout":
                bootstrap.option(ChannelOption.SO_TIMEOUT, options.getInteger("soTimeout"));
                break;
            case "tcpnodelay":
                bootstrap.option(ChannelOption.TCP_NODELAY, options.getBoolean("tcpNodelay"));
                break;
            default:
            }
        }
    }

    public static void applyOptions(@NonNull BObject options, @NonNull AbstractBootstrap<?, ?> bootstrap) {

        /*
         * generic option
         */

        // default so_linger = 0
        // options.putAnyIfAbsent("soLinger", 0);

        for (String name : options.keySet()) {
            applyOption(name, options, bootstrap);
        }

        /*
         * options for client (active/connect side)
         */
        if (bootstrap instanceof Bootstrap) {
            if (options.containsKey("connectTimeout")) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getInteger("connectTimeout"));
            }
        }

        /*
         * options for server (passive/bind side)
         */
        if (bootstrap instanceof ServerBootstrap) {

        }
    }
}
