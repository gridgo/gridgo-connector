package io.gridgo.connector.mysql;


import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.connector.mysql.support.JdbcOperationException;
import io.gridgo.connector.support.transaction.AbstractTransaction;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;

import java.util.HashMap;
import java.util.Map;

import static io.gridgo.connector.mysql.JdbcConstants.*;

@Slf4j
class JdbcTransaction extends AbstractTransaction {

    JdbcTransaction(Handle handle){
        this.handle = handle;
        bindHandlers();
    }
    private final Handle handle;

    interface TransactionHandler {
        Message handle(Message msg);
    }

    private Map<String, TransactionHandler> operationsMap = new HashMap<>();

    private void bindHandlers() {
        bind(OPERATION_SELECT, this::select);
        bind(OPERATION_UPDATE, this::update);
        bind(OPERATION_DELETE, this::update);
        bind(OPERATION_INSERT, this::update);
        bind(OPERATION_EXCUTE, this::execute);
    }
    private void bind(String name, TransactionHandler handler) {
        operationsMap.put(name, handler);
    }

    private Message select(Message msg){
        return JdbcOperator.select(msg, handle);
    }

    private Message update(Message msg){
        return JdbcOperator.updateRow(msg, handle);
    }

    private Message execute(Message msg){
        return JdbcOperator.execute(msg, handle);
    }

    private TransactionHandler getHandler(Message request){
        var operation = request.headers().getString(OPERATION);
        return operationsMap.get(operation);
    }



    @Override
    protected Promise<Message, Exception> doCommit() {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try (handle){
            handle.commit();
            Helper.ack(deferred, null, null);
        }
        return deferred;
    }

    @Override
    protected Promise<Message, Exception> doRollback() {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try(handle){
            handle.rollback();
            Helper.ack(deferred, null, null);
        }
        return deferred;
    }

    @Override
    public void send(Message message) {
        var handler = getHandler(message);
        if (handler == null){
            return;
        }
        try {
            handler.handle(message);
        } catch (Exception ex) {
            log.error("Error while processing JDBC Transaction request", ex);
        }
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var handler = getHandler(message);
        if (handler == null){
            return new SimpleFailurePromise<>(new JdbcOperationException());
        }
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try {
            handler.handle(message);
            Helper.ack(deferred, Message.ofEmpty(), null);
        } catch (Exception ex) {
            log.error("Error while processing JDBC Transaction request", ex);
            Helper.ack(deferred, null, ex);
        }
        return  deferred.promise();
    }

    @Override
    public Promise<Message, Exception> call(Message message) {
        var handler = getHandler(message);
        if (handler == null){
            return new SimpleFailurePromise<>(new JdbcOperationException());
        }
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try {
            Message result = handler.handle(message);
            Helper.ack(deferred, result, null);
        } catch (Exception ex) {
            log.error("Error while processing JDBC Transaction request", ex);
            Helper.ack(deferred, null, ex);
        }
        return  deferred.promise();
    }
}
