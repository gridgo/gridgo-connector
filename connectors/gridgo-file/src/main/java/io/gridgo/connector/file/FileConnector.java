package io.gridgo.connector.file;

import java.util.Optional;

import io.gridgo.connector.file.support.engines.BasicFileProducerEngine;
import io.gridgo.connector.file.support.engines.DisruptorFileProducerEngine;
import io.gridgo.connector.file.support.engines.FileProducerEngine;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "file", syntax = "[{engine}:]//{path}")
public class FileConnector extends AbstractConnector {

	public static final int DEFAULT_RINGBUFFER_SIZE = 1024;

	public static final int DEFAULT_MAX_BATCH_SIZE = 1000;

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

	private static final long DEFAULT_LIMIT = -1;

	private static final int DEFAULT_COUNT = 10;

	@Override
	protected void onInit() {
		var engineName = getPlaceholder("engine");

		var format = getParam("format", "raw");

		var lengthPrepend = !"false".equals(getParam("lengthPrepend"));

		var producerOnly = "true".equals(getParam("producerOnly"));

		var strBufferSize = getParam("bufferSize");
		var bufferSize = strBufferSize != null ? Integer.parseInt(strBufferSize) : DEFAULT_BUFFER_SIZE;

		FileProducerEngine engine = null;
		if ("disruptor".equals(engineName)) {
			engine = createDisruptorProducer(format, bufferSize, lengthPrepend);
		} else if (engineName == null || "basic".equals(engineName)) {
			engine = createBasicProducer(format, bufferSize, lengthPrepend);
		} else {
			throw new IllegalArgumentException("Unsupported file producer engine: " + engineName);
		}
		var path = getPlaceholder("path");
		var mode = getParam("mode", "rw");
		var deleteOnStartup = "true".equals(getParam("deleteOnStartup"));
		var deleteOnShutdown = "true".equals(getParam("deleteOnShutdown"));

		var strLimit = getParam("limitSize");
		var limit = strLimit != null ? Long.parseLong(strLimit) : DEFAULT_LIMIT;

		var strCount = getParam("rotationCount");
		var count = strCount != null ? Integer.parseInt(strCount) : DEFAULT_COUNT;

		var producer = new FileProducer(getContext(), path, mode, engine, deleteOnStartup, deleteOnShutdown, limit,
				count);
		this.producer = Optional.of(producer);
		if (!producerOnly)
			this.consumer = Optional.of(new FileConsumer(getContext(), path, format, bufferSize, lengthPrepend));
	}

	private FileProducerEngine createBasicProducer(String format, int bufferSize, boolean lengthPrepend) {
		return new BasicFileProducerEngine(getContext(), format, bufferSize, lengthPrepend);
	}

	private FileProducerEngine createDisruptorProducer(String format, int bufferSize, boolean lengthPrepend) {
		var strRingBufferSize = getParam("ringBufferSize");
		var strMaxBatchSize = getParam("maxBatchSize");

		var ringBufferSize = strRingBufferSize != null ? Integer.parseInt(strRingBufferSize) : DEFAULT_RINGBUFFER_SIZE;
		var maxBatchSize = strMaxBatchSize != null ? Integer.parseInt(strMaxBatchSize) : DEFAULT_MAX_BATCH_SIZE;

		var batchingEnabled = "true".equals(getParam("batchingEnabled"));

		return new DisruptorFileProducerEngine(getContext(), format, bufferSize, ringBufferSize, batchingEnabled,
				maxBatchSize, lengthPrepend);
	}
}
