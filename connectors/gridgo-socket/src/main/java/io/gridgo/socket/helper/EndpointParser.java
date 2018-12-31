package io.gridgo.socket.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.gridgo.utils.InetAddressUtils;

public class EndpointParser {

    private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");
    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("(?i)^(.+):\\/\\/(.+)$");

    private static String[] extractToSegments(String address) {
        if (address == null)
            return null;
        Matcher matcher = ENDPOINT_PATTERN.matcher(address.trim());
        if (!matcher.find())
            return null;
        String protocol = matcher.group(1);
        String hostAndPort = matcher.group(2).trim();
        String[] arrMayContainsInterface = hostAndPort.split(";");
        String nic = null;
        String[] arr = null;
        if (arrMayContainsInterface.length == 1) {
            arr = hostAndPort.split(":");
        } else {
            nic = arrMayContainsInterface[0];
            arr = arrMayContainsInterface[1].split(":");
        }

        if (arr.length > 1) {
            String maybePort = arr[arr.length - 1];
            if (PORT_PATTERN.matcher(maybePort).find()) {
                StringBuilder host = new StringBuilder();
                for (int i = 0; i < arr.length - 1; i++) {
                    if (host.length() > 0) {
                        host.append(":");
                    }
                    host.append(arr[i]);
                }
                return new String[] { protocol, host.toString(), maybePort, nic };
            }
        }
        return new String[] { protocol, hostAndPort, null, nic };
    }

    public static Endpoint parse(String address) {
        if (address != null) {
            String[] segments = extractToSegments(address.trim());
            if (segments == null) {
                throw new IllegalArgumentException("Invalid address: " + address);
            }

            String protocol = segments[0].toLowerCase();
            String host = segments[1];

            if (!host.equals("*")) {
                String resolvedHost = InetAddressUtils.resolve(host);
                host = resolvedHost == null ? host : resolvedHost;
            }

            int port = segments[2] == null ? -1 : Integer.valueOf(segments[2]);
            String nic = segments[3];

            return Endpoint.builder().nic(nic).address(address).protocol(protocol).host(host).port(port).build();
        }
        throw new IllegalArgumentException("Invalid address: " + address);
    }
}
