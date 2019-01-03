package io.gridgo.connector.file;

import java.io.IOException;
import java.util.Optional;

import io.gridgo.connector.file.support.engines.BasicFileProducerEngine;
import io.gridgo.connector.file.support.engines.DisruptorFileProducerEngine;
import io.gridgo.connector.file.support.engines.FileProducerEngine;
import io.gridgo.connector.file.support.exceptions.FileInitException;
import io.gridgo.connector.file.support.limit.AutoIncrementedFileLimitStrategy;
import io.gridgo.connector.file.support.limit.FileLimitStrategy;
import io.gridgo.connector.file.support.limit.NoLimitStrategy;
import io.gridgo.connector.file.support.limit.RotatingFileLimitStrategy;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "file", syntax = "[{engine}:]//{path}")
public class FileConnector extends AbstractConnector {

    public static final int DEFAULT_RINGBUFFER_SIZE = 1024;

    public static final int DEFAULT_MAX_BATCH_SIZE = 1000;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    private static final long DEFAULT_LIMIT = -1;

    private static final int DEFAULT_COUNT = 10;

    private FileProducerEngine engine;

    private FileLimitStrategy limitStrategy;

    private FileProducerEngine createBasicProducer(String format, int bufferSize, boolean lengthPrepend) {
        return new BasicFileProducerEngine(getContext(), format, bufferSize, lengthPrepend);
    }

    private FileProducerEngine createDisruptorProducer(String format, int bufferSize, boolean lengthPrepend) {
        var strRingBufferSize = getParam("ringBufferSize");
        var strMaxBatchSize = getParam("maxBatchSize");

        var ringBufferSize = strRingBufferSize != null ? Integer.parseInt(strRingBufferSize) : DEFAULT_RINGBUFFER_SIZE;
        var maxBatchSize = strMaxBatchSize != null ? Integer.parseInt(strMaxBatchSize) : DEFAULT_MAX_BATCH_SIZE;

        var batchingEnabled = "true".equals(getParam("batchingEnabled"));

        return new DisruptorFileProducerEngine(getContext(), format, bufferSize, ringBufferSize, batchingEnabled, maxBatchSize, lengthPrepend);
    }

    private FileLimitStrategy createLimitStrategy(String limitStrategy, String path, String mode, long limit, int count, boolean deleteOnStartup,
            boolean deleteOnShutdown, boolean override) {
        try {
            if (limitStrategy == null)
                return new NoLimitStrategy(path, mode, deleteOnStartup, deleteOnShutdown, override);
            if (limitStrategy.equals("rotate"))
                return new RotatingFileLimitStrategy(path, mode, limit, count, deleteOnStartup, deleteOnShutdown, override);
            if (limitStrategy.equals("autoincrement"))
                return new AutoIncrementedFileLimitStrategy(path, mode, limit, deleteOnStartup, deleteOnShutdown, override);
        } catch (IOException ex) {
            throw new FileInitException("Cannot create limit strategy", ex);
        }
        throw new UnsupportedOperationException("Limit Strategy is unsupported: " + limitStrategy);
    }

    @Override
    protected void onInit() {
        var engineName = getPlaceholder("engine");

        var format = getParam("format", "raw");

        var lengthPrepend = !"false".equals(getParam("lengthPrepend"));

        var producerOnly = "true".equals(getParam("producerOnly"));

        var strBufferSize = getParam("bufferSize");
        var bufferSize = strBufferSize != null ? Integer.parseInt(strBufferSize) : DEFAULT_BUFFER_SIZE;

        if ("disruptor".equals(engineName)) {
            this.engine = createDisruptorProducer(format, bufferSize, lengthPrepend);
        } else if (engineName == null || "basic".equals(engineName)) {
            this.engine = createBasicProducer(format, bufferSize, lengthPrepend);
        } else {
            throw new IllegalArgumentException("Unsupported file producer engine: " + engineName);
        }
        var path = getPlaceholder("path");
        var mode = getParam("mode", "rw");
        var override = "true".equals(getParam("override"));
        var deleteOnStartup = "true".equals(getParam("deleteOnStartup"));
        var deleteOnShutdown = "true".equals(getParam("deleteOnShutdown"));

        var strLimit = getParam("limitSize");
        var limit = strLimit != null ? Long.parseLong(strLimit) : DEFAULT_LIMIT;

        var strCount = getParam("rotationCount");
        var count = strCount != null ? Integer.parseInt(strCount) : DEFAULT_COUNT;

        var strLimitStrategy = getParam("limitStrategy");
        this.limitStrategy = createLimitStrategy(strLimitStrategy, path, mode, limit, count, deleteOnStartup, deleteOnShutdown, override);

        var producer = new FileProducer(getContext(), path, engine);
        this.producer = Optional.of(producer);
        if (!producerOnly) {
            this.consumer = Optional.of(new FileConsumer(getContext(), path, format, bufferSize, lengthPrepend, limitStrategy));
        }
    }

    @Override
    protected void onStart() {
        try {
            this.limitStrategy.start();
        } catch (IOException e) {
            throw new FileInitException("Cannot start limit strategy", e);
        }
        this.engine.setLimitStrategy(limitStrategy);
        this.engine.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.engine.stop();
        try {
            this.limitStrategy.stop();
        } catch (IOException e) {
            throw new FileInitException("Cannot stop limit strategy", e);
        }
    }
}
