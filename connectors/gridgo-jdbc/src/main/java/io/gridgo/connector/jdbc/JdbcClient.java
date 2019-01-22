package io.gridgo.connector.jdbc;

import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.transaction.AbstractTransaction;
import io.gridgo.framework.support.Message;
import org.jdbi.v3.core.Handle;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import java.util.HashMap;
import java.util.Map;

import static io.gridgo.connector.jdbc.JdbcConstants.*;

public abstract class JdbcClient extends AbstractTransaction {
    interface JdbcClientHandler {
        Message handle(Message msg, Handle handle);
    }

    Map<String, JdbcClientHandler> operationsMap = new HashMap<>();

    JdbcClient(ConnectorContext context) {
        super(context);
        bindHandlers();
    }

    protected abstract Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC);

    private void bind(String name, JdbcClientHandler handler) {
        operationsMap.put(name, handler);
    }

    private void bindHandlers() {
        bind(OPERATION_SELECT, JdbcOperator::select);
        bind(OPERATION_UPDATE, JdbcOperator::updateRow);
        bind(OPERATION_DELETE, JdbcOperator::updateRow);
        bind(OPERATION_INSERT, JdbcOperator::updateRow);
        bind(OPERATION_EXCUTE, JdbcOperator::execute);
    }

    @Override
    protected Promise<Message, Exception> doCommit() {
        return null;
    }

    @Override
    protected Promise<Message, Exception> doRollback() {
        return null;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    @Override
    protected void onStart() {
        // Nothing to do here
    }

    @Override
    protected void onStop() {
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(request, deferred, true);
    }

    @Override
    public void send(Message message) {
        _call(message, null, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(message, deferred, false);
    }

    @Override
    protected String generateName() {
        return null;
    }
}
