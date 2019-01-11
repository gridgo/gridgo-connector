package io.gridgo.connector.mysql;


import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;
import snaq.db.ConnectionPool;

import java.util.HashMap;
import java.util.Map;

import static io.gridgo.connector.mysql.MySQLConstants.*;

@Slf4j
public class MySQLProducer extends AbstractProducer {

    interface ProducerHandler {
        void handle(Message msg, Handle handle, Deferred<Message, Exception> deferred);
    }

    private Map<String, ProducerHandler> operationsMap = new HashMap<>();

    private ConnectionPool connectionPool;

    MySQLProducer(ConnectorContext context, String url, String userName, String password) {
        super(context);
        this.connectionPool = new ConnectionPool("local", 5, 10, 30, 180, url, userName, password);
        bindHandlers();
    }

    private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        if(deferred == null){
            return null;
        }
        var operation = request.headers().getString(OPERATION);
        var handler = operationsMap.get(operation);
        if (BEGIN_TRANSACTION.equals(operation)){
            handler.handle(request, Jdbi.create(connectionPool::getConnection).open(), deferred);
        }else {
            try(Handle handle = Jdbi.create(connectionPool::getConnection).open()) {
                handler.handle(request, handle, deferred);
            } catch (Exception ex) {
                log.error("Error while processing MySQL request", ex);
                return new SimpleFailurePromise<>(ex);
            }
        }
        return  deferred.promise();
    }

    private void bind(String name, ProducerHandler handler) {
        operationsMap.put(name, handler);
    }

    private void bindHandlers() {
        bind(OPERATION_SELECT, MySQLOperator::select);
        bind(OPERATION_UPDATE, MySQLOperator::updateRow);
        bind(OPERATION_DELETE, MySQLOperator::updateRow);
        bind(OPERATION_INSERT, MySQLOperator::updateRow);
        bind(OPERATION_EXCUTE, MySQLOperator::execute);
        bind(BEGIN_TRANSACTION, MySQLOperator::begin);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(request, deferred, true);
    }

    @Override
    protected String generateName() {
        return "mySQL";
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
        connectionPool.release();
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

}
