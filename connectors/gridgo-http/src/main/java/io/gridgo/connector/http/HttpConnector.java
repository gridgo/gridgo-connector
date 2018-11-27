package io.gridgo.connector.http;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Realm;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyType;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.netty.resolver.NameResolver;

@ConnectorEndpoint(scheme = "http,https", syntax = "httpUri", raw = true)
public class HttpConnector extends AbstractConnector {

	private static final int DEFAULT_MAX_REDIRECTS = 3;
	private static final int DEFAULT_PROXY_PORT = 80;

	protected void onInit() {
		var endpoint = getConnectorConfig().getNonQueryEndpoint();
		var config = buildConfiguration();
		var format = getParam(HttpConstants.PARAM_FORMAT);
		var method = getParam(HttpConstants.PARAM_METHOD);
		var nameResolver = getNameResolver();
		this.producer = Optional.of(new HttpProducer(getContext(), endpoint, config, format, nameResolver, method));
	}

	private Builder buildConfiguration() {
		var config = Dsl.config();
		var connectTimeout = getParam(HttpConstants.CONNECT_TIMEOUT);
		if (connectTimeout != null)
			config.setConnectTimeout(Integer.parseInt(connectTimeout));
		var requestTimeout = getParam(HttpConstants.REQUEST_TIMEOUT);
		if (requestTimeout != null)
			config.setRequestTimeout(Integer.parseInt(requestTimeout));
		var maxRetries = getParam(HttpConstants.MAX_RETRIES);
		if (maxRetries != null)
			config.setMaxRequestRetry(Integer.parseInt(maxRetries));
		var maxConnections = getParam(HttpConstants.MAX_CONNECTIONS);
		if (maxConnections != null)
			config.setMaxConnections(Integer.parseInt(maxConnections));
		var maxRedirects = getParam(HttpConstants.MAX_REDIRECTS);
		if (maxRedirects != null)
			config.setMaxRedirects(Integer.parseInt(maxRedirects));
		else
			config.setMaxRedirects(DEFAULT_MAX_REDIRECTS);
		var keepAlive = getParam(HttpConstants.KEEP_ALIVE);
		if (keepAlive != null)
			config.setKeepAlive(Boolean.valueOf(keepAlive));
		var compression = getParam(HttpConstants.COMPRESSION);
		if (compression != null)
			config.setCompressionEnforced(Boolean.valueOf(compression));
		var ioThreadsCount = getParam(HttpConstants.IO_THREADS_COUNT);
		if (ioThreadsCount != null)
			config.setIoThreadsCount(Integer.parseInt(ioThreadsCount));
		var useProxy = getParam(HttpConstants.USE_PROXY);
		if (Boolean.valueOf(useProxy)) {
			var host = getParam(HttpConstants.PROXY_HOST);
			var port = getParam(HttpConstants.PROXY_PORT);
			var securedPort = getParam(HttpConstants.PROXY_SECURED_PORT);
			var nonProxyHosts = getParam(HttpConstants.NON_PROXY_HOSTS);
			var proxyType = getParam(HttpConstants.PROXY_TYPE);
			var realmBean = getParam(HttpConstants.PROXY_REALM_BEAN);
			config.setProxyServer(new ProxyServer( //
					host, //
					port != null ? Integer.parseInt(port) : DEFAULT_PROXY_PORT,
					securedPort != null ? Integer.parseInt(securedPort) : DEFAULT_PROXY_PORT, //
					realmBean != null ? getContext().getRegistry().lookupMandatory(realmBean, Realm.class) : null, //
					nonProxyHosts != null ? Arrays.asList(nonProxyHosts.split(",")) : Collections.emptyList(),
					proxyType != null ? ProxyType.valueOf(proxyType) : ProxyType.HTTP));
		}
		return config;
	}

	private NameResolver<InetAddress> getNameResolver() {
		var nameResolver = getNameResolverByClass();
		if (nameResolver != null)
			return nameResolver;
		return getNameResolverByBean();
	}

	@SuppressWarnings("unchecked")
	private NameResolver<InetAddress> getNameResolverByBean() {
		var nameResolverBean = getParam(HttpConstants.NAME_RESOLVER_BEAN);
		if (nameResolverBean == null)
			return null;
		return getContext().getRegistry().lookupMandatory(nameResolverBean, NameResolver.class);
	}

	@SuppressWarnings("unchecked")
	private NameResolver<InetAddress> getNameResolverByClass() {
		var nameResolverClass = getParam(HttpConstants.NAME_RESOLVER_CLASS);
		if (nameResolverClass == null)
			return null;
		try {
			return (NameResolver<InetAddress>) Class.forName(nameResolverClass).getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
