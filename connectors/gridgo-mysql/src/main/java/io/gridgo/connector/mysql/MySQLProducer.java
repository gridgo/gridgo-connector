package io.gridgo.connector.mysql;


import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mysql.support.MySQLOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.ConnectionFactory;
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
        Message handle(Message msg, Handle handle);
    }

    private Map<String, ProducerHandler> operationsMap = new HashMap<>();
    private Jdbi jdbiClient;


    MySQLProducer(ConnectorContext context, ConnectionFactory connectionFactory) {
        super(context);
        this.jdbiClient = Jdbi.create(connectionFactory);
        bindHandlers();
    }



    private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        if(deferred == null){
            return null;
        }
        var operation = request.headers().getString(OPERATION);
        var handler = operationsMap.get(operation);
        if (handler == null){
            return new SimpleFailurePromise<>(new MySQLOperationException());
        }
        if (BEGIN_TRANSACTION.equals(operation)){
            handler.handle(request, jdbiClient.open());
        }else {
            try(Handle handle = jdbiClient.open()) {
                Message result = handler.handle(request, handle);
                ack(deferred, result);
            } catch (Exception ex) {
                log.error("Error while processing MySQL request", ex);
                ack(deferred, ex);
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
