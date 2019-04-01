package io.gridgo.connector.keyvalue;

import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_DELETE;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET_ALL;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_SET;

import java.util.HashMap;
import java.util.Map;

import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;

@Getter
public abstract class AbstractKeyValueProducer extends AbstractProducer implements KeyValueProducer {

    private Map<String, ProducerHandler> operations = new HashMap<>();

    public AbstractKeyValueProducer(ConnectorContext context) {
        super(context);
        bindHandlers();
    }

    private void bindHandlers() {
        operations.put(OPERATION_SET, this::putValue);
        operations.put(OPERATION_GET, this::getValue);
        operations.put(OPERATION_GET_ALL, this::getAll);
        operations.put(OPERATION_DELETE, this::delete);
    }

    @Override
    public void send(Message message) {
        process(message, false, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return process(message, true, false);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        return process(request, true, true);
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }
}
