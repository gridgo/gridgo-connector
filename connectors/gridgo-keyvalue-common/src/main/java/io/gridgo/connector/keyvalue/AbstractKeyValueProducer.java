package io.gridgo.connector.keyvalue;

import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_DELETE;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET_ALL;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_SET;

import java.util.HashMap;
import java.util.Map;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractKeyValueProducer extends AbstractProducer {

    private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new DefaultExecutionStrategy();

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
        _call(message, false, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return _call(message, true, false);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        return _call(request, true, true);
    }

    private Promise<Message, Exception> _call(Message message, boolean deferredRequired, boolean isRPC) {
        // get the operation and associated handler
        var operation = message.headers().getString(OPERATION);
        var handler = operations.get(operation);
        if (handler == null) {
            return Promise.ofCause(new IllegalArgumentException("Operation " + operation + " is not supported"));
        }

        // call the handler with deferred if required
        var deferred = deferredRequired ? new CompletableDeferredObject<Message, Exception>() : null;
        var strategy = getContext().getProducerExecutionStrategy().orElse(DEFAULT_EXECUTION_STRATEGY);

        strategy.execute(() -> {
            try {
                handler.handle(message, deferred, isRPC);
            } catch (Exception ex) {
                log.error("Exception caught while executing handler", ex);
                deferred.reject(ex);
            }
        });
        return deferred != null ? deferred.promise() : null;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    protected void putValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception {
        putValue(message, message.body().asObject(), deferred, isRPC);
    }

    protected void delete(Message message, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception {
        delete(message, message.body().asValue(), deferred, isRPC);
    }

    protected void getValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception {
        getValue(message, message.body().asValue(), deferred, isRPC);
    }

    protected abstract void putValue(Message message, BObject body, Deferred<Message, Exception> deferred,
            boolean isRPC) throws Exception;

    protected abstract void delete(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;

    protected abstract void getValue(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;

    protected abstract void getAll(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;
}
