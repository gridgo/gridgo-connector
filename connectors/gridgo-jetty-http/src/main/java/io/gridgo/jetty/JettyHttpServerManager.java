package io.gridgo.jetty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.utils.ThreadUtils;
import io.gridgo.utils.support.HostAndPort;
import lombok.Getter;
import lombok.NonNull;

public class JettyHttpServerManager {

	@Getter
	private static final JettyHttpServerManager instance = new JettyHttpServerManager();

	private static final String ALL_INTERFACE_HOST = "0.0.0.0";

	private final Map<HostAndPort, JettyHttpServer> servers = new NonBlockingHashMap<>();

	private JettyHttpServerManager() {
		ThreadUtils.registerShutdownTask(this::onShutdown);
	}

	private void onShutdown() {
		Collection<JettyHttpServer> runningServers = new LinkedList<>(servers.values());
		for (JettyHttpServer server : runningServers) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void onServerStop(HostAndPort address) {
		this.servers.remove(address);
	}

	public JettyHttpServer getOrCreateJettyServer(String address, Set<JettyServletContextHandlerOption> options) {
		return this.getOrCreateJettyServer(HostAndPort.fromString(address), options);
	}

	public JettyHttpServer getOrCreateJettyServer(String address) {
		return this.getOrCreateJettyServer(HostAndPort.fromString(address), null);
	}

	public JettyHttpServer getOrCreateJettyServer(@NonNull HostAndPort originAddress) {
		return getOrCreateJettyServer(originAddress, null);
	}

	public JettyHttpServer getOrCreateJettyServer(@NonNull HostAndPort originAddress,
			Set<JettyServletContextHandlerOption> options) {
		HostAndPort address = originAddress.makeCopy();
		if (!address.isResolvable()) {
			throw new RuntimeException("Host '" + originAddress.getHost() + "' cannot be resolved");
		}

		if (address.getPort() <= 0) {
			address.setPort(80);
		}

		if (address.getHost() == null) {
			address.setHost("localhost");
		}

		JettyHttpServer jettyHttpServer = servers.get(address);
		if (jettyHttpServer == null) {
			HostAndPort allInterface = HostAndPort.newInstance(ALL_INTERFACE_HOST, address.getPort());
			jettyHttpServer = servers.get(allInterface);
			if (jettyHttpServer == null) {
				synchronized (this.servers) {
					if (!this.servers.containsKey(address) && !this.servers.containsKey(allInterface)) {
						jettyHttpServer = new JettyHttpServer(address, options, this::onServerStop);
						this.servers.put(address, jettyHttpServer);
					}
				}
			}
		}
		return jettyHttpServer;
	}
}
