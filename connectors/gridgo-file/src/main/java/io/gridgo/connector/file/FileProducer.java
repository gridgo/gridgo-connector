package io.gridgo.connector.file;

import org.joo.promise4j.Promise;

import io.gridgo.connector.file.support.engines.FileProducerEngine;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

public class FileProducer extends AbstractProducer {

    private String path;

    @Getter
    private FileProducerEngine engine;

    public FileProducer(ConnectorContext context, String path, FileProducerEngine engine) {
        super(context);
        this.engine = engine;
        this.path = path;
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        throw new UnsupportedOperationException("File doesn't support call");
    }

    @Override
    protected String generateName() {
        return "producer.file:" + engine.getName() + "." + path;
    }

    @Override
    public boolean isCallSupported() {
        return false;
    }

    @Override
    protected void onStart() {
        // Nothing to do here
    }

    @Override
    protected void onStop() {
        // Nothing to do here
    }

    @Override
    public void send(Message message) {
        this.engine.send(message);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return this.engine.sendWithAck(message);
    }
}
