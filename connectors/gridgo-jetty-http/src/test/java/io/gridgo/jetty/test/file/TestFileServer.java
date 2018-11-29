package io.gridgo.jetty.test.file;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.jetty.support.HttpConstants;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.config.impl.DefaultConnectorContextBuilder;
import io.gridgo.framework.execution.impl.ExecutorExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.ThreadUtils;

public class TestFileServer {
	private static final ConnectorResolver resolver = new ClasspathConnectorResolver("io.gridgo.connector");

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private Connector connector;
	private Consumer consumer;
	private Producer responder;

	private File rootDir;

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("Usage: provide argument 1 as <endpoint>, example: http://localhost:8080/*");
			return;
		}

		String endpoint = args[0];
		String directory = args.length > 1 ? args[1] : "test-resources";

		final var app = new TestFileServer(endpoint, directory);
		ThreadUtils.registerShutdownTask(() -> {
			app.stop();
		});
		app.start();
	}

	private TestFileServer(String endpoint, String directory) {
		System.out.println("Http server listen on: " + endpoint + ", root dir: " + directory);

		this.rootDir = new File(directory);

		if (!rootDir.exists()) {
			throw new RuntimeException("Root directory at: " + rootDir.getAbsolutePath() + " is not exists");
		}

		ConnectorContext connectorContext = new DefaultConnectorContextBuilder() //
				.setCallbackInvokerStrategy(new ExecutorExecutionStrategy(executor)) //
				.setExceptionHandler((ex) -> {
					ex.printStackTrace();
				}) //
				.build();

		connector = resolver.resolve("jetty:" + endpoint, connectorContext);
	}

	private void start() {
		connector.start();

		consumer = connector.getConsumer().get();
		responder = connector.getProducer().get();

		consumer.subscribe(this::onRequest);
	}

	private void onRequest(Message message) {
		System.out.println("Got message payload: " + message.getPayload().toBArray());

		BObject headers = BObject.newDefault();
		BElement body = null;

		String pathInfo = message.getPayload().getHeaders().getString(HttpConstants.PATH_INFO);
		if (pathInfo != null) {
			File file = new File(rootDir, pathInfo);
			if (file.exists() && file.isFile()) {
				body = BReference.newDefault(file);
			}
		}

		if (body == null) {
			headers.setAny(HttpConstants.HTTP_STATUS, "404");
			body = BValue.newDefault("Not found");
		}

		Payload payload = Payload.newDefault(headers, body);
		Message response = Message.newDefault(payload).setRoutingIdFromAny(message.getRoutingId().get());
		this.responder.send(response);
	}

	private void stop() {
		try {
			this.consumer.clearSubscribers();
			this.connector.stop();
		} finally {
			this.connector = null;
			this.consumer = null;
			this.responder = null;
		}
	}
}
