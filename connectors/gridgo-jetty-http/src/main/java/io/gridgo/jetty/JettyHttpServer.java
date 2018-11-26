package io.gridgo.jetty;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.gridgo.framework.NonameComponentLifecycle;
import io.gridgo.utils.support.HostAndPort;
import lombok.Getter;
import lombok.NonNull;

public class JettyHttpServer extends NonameComponentLifecycle {

	private Server server;

	@Getter
	private final HostAndPort address;

	private ServletContextHandler handler;

	private final Consumer<HostAndPort> onStopCallback;

	private final Set<JettyServletContextHandlerOption> options;

	JettyHttpServer(@NonNull HostAndPort address, Set<JettyServletContextHandlerOption> options,
			Consumer<HostAndPort> onStopCallback) {
		this.address = address;
		this.onStopCallback = onStopCallback;
		this.options = options;
	}

	public JettyHttpServer addPathHandler(@NonNull String path,
			@NonNull BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
		ServletHolder servletHolder = new ServletHolder(new DelegateServlet(handler));
		servletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(path));
		this.handler.addServlet(servletHolder, path);
		return this;
	}

	@Override
	protected void onStart() {
		server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setHost(address.getResolvedIp());
		connector.setPort(address.getPort());
		server.addConnector(connector);

		handler = createServletContextHandler();
		server.setHandler(handler);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("Cannot start server connector for host: " + address, e);
		}
	}

	private ServletContextHandler createServletContextHandler() {
		if (this.options == null || this.options.size() == 0) {
			return new ServletContextHandler();
		} else {
			int options = 0;
			for (JettyServletContextHandlerOption option : this.options) {
				options = options | option.getCode();
			}
			return new ServletContextHandler(options);
		}
	}

	@Override
	protected void onStop() {
		try {
			this.server.stop();
		} catch (Exception e) {
			getLogger().error("Error while stop jetty server", e);
		} finally {
			if (this.onStopCallback != null) {
				this.onStopCallback.accept(this.address);
			}
			this.server = null;
			this.handler = null;
		}
	}

}
