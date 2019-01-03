package io.gridgo.connector.file.support.engines;

import io.gridgo.connector.file.FileConsumer;

public class SimpleFileConsumerEngine implements FileConsumerEngine {

    private FileConsumer fileConsumer;

    public SimpleFileConsumerEngine(FileConsumer fileConsumer) {
        this.fileConsumer = fileConsumer;
    }

    @Override
    public String getFormat() {
        return this.fileConsumer.getFormat();
    }

    @Override
    public void readAndPublish() {
        throw new UnsupportedOperationException("Non-length-prepended mode is not supported for FileConsumer");
    }
}
