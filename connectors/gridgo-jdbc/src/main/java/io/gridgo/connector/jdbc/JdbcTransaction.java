package io.gridgo.connector.jdbc;


import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.jdbc.support.JdbcOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.transaction.TransactionException;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;


import static io.gridgo.connector.jdbc.JdbcConstants.*;

@Slf4j
class JdbcTransaction extends JdbcClient {
    private AbstractProducer producer;

    JdbcTransaction(Handle handle, ConnectorContext context) {
        super(context);
        this.handle = handle;
    }

    private final Handle handle;

    @Override
    protected String generateName() {
        return null;
    }

    @Override
    protected Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
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
        }catch (Exception ex){
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

    }

    @Override
    protected void onStop() {

    }

}
