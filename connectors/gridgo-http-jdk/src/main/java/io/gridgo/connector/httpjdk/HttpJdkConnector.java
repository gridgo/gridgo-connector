package io.gridgo.connector.httpjdk;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.Optional;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "http2,https2", syntax = "httpUri", raw = true)
public class HttpJdkConnector extends AbstractConnector {

    protected void onInit() {
        var endpoint = parseEndpoint();
        var format = getParam(HttpJdkConstants.PARAM_FORMAT);
        var method = getParam(HttpJdkConstants.PARAM_METHOD);
        var builder = createBuilder();
        this.producer = Optional.of(new HttpJdkProducer(getContext(), builder, endpoint, format, method));
    }

    private HttpClient.Builder createBuilder() {
        var builder = HttpClient.newBuilder();

        // connection timeout
        var connectTimeout = getParam(HttpJdkConstants.CONNECT_TIMEOUT);
        if (connectTimeout != null)
            builder.connectTimeout(Duration.ofMillis(Integer.parseInt(connectTimeout)));

        // request priority
        var priority = getParam(HttpJdkConstants.PARAM_PRIORITY);
        if (priority != null)
            builder.priority(Integer.parseInt(priority));

        builder.version(Version.HTTP_2);

        var redirect = getParam(HttpJdkConstants.PARAM_REDIRECT);
        if (redirect != null)
            builder.followRedirects(Redirect.valueOf(redirect));

        var useProxy = getParam(HttpJdkConstants.USE_PROXY);
        if ("true".equals(useProxy)) {
            var proxyHost = getParam(HttpJdkConstants.PROXY_HOST);
            var proxyPort = getParam(HttpJdkConstants.PROXY_PORT);

            var port = proxyPort != null ? Integer.parseInt(proxyPort) : HttpJdkConstants.DEFAULT_PROXY_PORT;

            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, port)));
        }

        return builder;
    }

    private String parseEndpoint() {
        var scheme = getConnectorConfig().getScheme();
        var endpoint = new StringBuilder();
        if (HttpJdkConstants.SCHEME_HTTP.equals(scheme))
            endpoint.append(HttpJdkConstants.PROTOCOL_HTTP);
        else
            endpoint.append(HttpJdkConstants.PROTOCOL_HTTPS);
        endpoint.append(":");
        endpoint.append(getConnectorConfig().getRemaining());
        return endpoint.toString();
    }
}
