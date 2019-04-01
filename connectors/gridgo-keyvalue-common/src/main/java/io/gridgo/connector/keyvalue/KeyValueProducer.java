package io.gridgo.connector.keyvalue;

import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION;

import java.util.Map;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.helper.Loggable;

public interface KeyValueProducer extends Loggable {

    public static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new DefaultExecutionStrategy();

    public default Promise<Message, Exception> process(Message message, boolean deferredRequired, boolean isRPC) {
        // get the operation and associated handler
        var operations = getOperations();
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
                getLogger().error("Exception caught while executing handler", ex);
                deferred.reject(ex);
            }
        });
        return deferred != null ? deferred.promise() : null;
    }

    public default void putValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception {
        putValue(message, message.body().asObject(), deferred, isRPC);
    }

    public default void delete(Message message, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception {
        delete(message, message.body().asValue(), deferred, isRPC);
    }

    public default void getValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception {
        getValue(message, message.body().asValue(), deferred, isRPC);
    }

    public void putValue(Message message, BObject body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;

    public void delete(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;

    public void getValue(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception;

    public void getAll(Message message, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception;

    public Map<String, ProducerHandler> getOperations();

    public ConnectorContext getContext();
}
