package io.gridgo.connector.file;

import io.gridgo.connector.file.support.engines.LengthPrependedFileConsumerEngine;
import io.gridgo.connector.file.support.engines.SimpleFileConsumerEngine;
import io.gridgo.connector.file.support.limit.FileLimitStrategy;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public class FileConsumer extends AbstractConsumer {

    @Getter
    private String path;

    @Getter
    private String format;

    private boolean lengthPrepend;

    @Getter
    private byte[] buffer;

    @Getter
    private boolean hasRotation;

    @Getter
    private int count;

    @Getter
    private FileLimitStrategy limitStrategy;

    public FileConsumer(ConnectorContext context, String path, String format, int bufferSize, boolean lengthPrepend, FileLimitStrategy limitStrategy) {
        super(context);
        this.path = path;
        this.format = format;
        this.lengthPrepend = lengthPrepend;
        this.buffer = new byte[bufferSize];
        this.limitStrategy = limitStrategy;
    }

    @Override
    protected String generateName() {
        return "consumer.file." + path;
    }

    @Override
    protected void onStart() {
        readAndPublish();
    }

    @Override
    protected void onStop() {
        // Nothing to do here
    }

    public void publishMessage(Message msg) {
        publish(msg, null);
    }

    private void readAndPublish() {
        var engine = lengthPrepend ? new LengthPrependedFileConsumerEngine(this) : new SimpleFileConsumerEngine(this);
        getContext().getConsumerExecutionStrategy().ifPresentOrElse(strategy -> strategy.execute(engine::readAndPublish), engine::readAndPublish);
    }
}
