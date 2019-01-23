package io.gridgo.connector.jdbc;

import static io.gridgo.connector.jdbc.JdbcConstants.OPERATION;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.transaction.TransactionException;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;

import io.gridgo.connector.jdbc.support.JdbcOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JdbcTransaction extends JdbcClient {

    private final Handle handle;

    JdbcTransaction(Handle handle, ConnectorContext context) {
        super(context);
        this.handle = handle;
    }

    @Override
    protected Promise<Message, Exception> doCall(Message request,
            CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        var operation = request.headers().getString(OPERATION);
        var handler = operationsMap.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(new JdbcOperationException());
        }
        try {
            Message result = handler.handle(request, handle);
            ack(deferred, result);
        } catch (Exception ex) {
            log.error("Error while processing JDBC Transaction request", ex);
            ack(deferred, ex);
        }
        return deferred == null ? null : deferred.promise();
    }

    @Override
    protected Promise<Message, Exception> doCommit() {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try (var temHandle = handle) {
            temHandle.commit();
            ack(deferred, Message.ofEmpty());
        } catch (TransactionException ex) {
            log.error("Commit error!!!", ex);
            ack(deferred, ex);
        }
        return deferred;
    }

    @Override
    protected Promise<Message, Exception> doRollback() {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try (var temHandle = handle) {
            temHandle.rollback();
            ack(deferred, Message.ofEmpty());
        } catch (Exception ex) {
            log.error("Rollback Error!!!", ex);
            ack(deferred, ex);
        }
        return deferred;
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
    protected String generateName() {
        return null;
    }
}
