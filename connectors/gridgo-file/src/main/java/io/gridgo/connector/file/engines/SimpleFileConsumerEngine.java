package io.gridgo.connector.file.engines;

import java.io.IOException;

import io.gridgo.connector.file.FileConsumer;

public class SimpleFileConsumerEngine implements FileConsumerEngine {

	private FileConsumer fileConsumer;

	public SimpleFileConsumerEngine(FileConsumer fileConsumer) {
		this.fileConsumer = fileConsumer;
	}

	@Override
	public void readAndPublish() throws IOException {
		throw new UnsupportedOperationException("Non-length-prepended mode is not supported for FileConsumer");
	}

	@Override
	public String getFormat() {
		return this.fileConsumer.getFormat();
	}
}
