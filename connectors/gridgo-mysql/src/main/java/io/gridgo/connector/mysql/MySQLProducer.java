package io.gridgo.connector.mysql;


import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mysql.support.Helper;
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class MySQLProducer extends AbstractProducer {

    interface ProducerHandler {

        public void handle(Message msg, Deferred<Message, Exception> deferred);
    }

    private Map<String, ProducerHandler> operationsMap = new HashMap<>();

    private ConnectionPool connectionPool;

    private AtomicInteger transactionId;

    private ConcurrentHashMap<Integer, Handle> transactionMap;

    public MySQLProducer(ConnectorContext context, String url, String userName, String password) {
        super(context);
        this.connectionPool = new ConnectionPool("local", 5, 10, 30, 180, url, userName, password);
        this.transactionId = new AtomicInteger(1);
        this.transactionMap = new ConcurrentHashMap<>();
        bindHandlers();
    }

    private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        if(deferred == null){
            return null;
        }
        var queryStatement = request.getPayload().getBody().asValue().getString();
        var operation = Helper.getOperation(queryStatement);
        var handler = operationsMap.get(operation);
        if (handler == null) {
            handler = this::execute;
        }
        try {
            handler.handle(request, deferred);
        } catch (Exception ex) {
            log.error("Error while processing MySQL request", ex);
            return new SimpleFailurePromise<>(ex);
        }
        return  deferred.promise();
    }


    private void ack(Deferred<Message, Exception> deferred, Object result, Throwable throwable) {
        if (deferred == null) {
            return;
        }
        deferred.resolve(Message.ofAny(result));
    }


    public void bind(String name, ProducerHandler handler) {
        operationsMap.put(name, handler);
    }

    private void bindHandlers() {
        bind(MySQLConstants.OPERATION_SELECT, this::select);
        bind(MySQLConstants.OPERATION_UPDATE, this::updateRow);
        bind(MySQLConstants.OPERATION_DELETE, this::updateRow);
        bind(MySQLConstants.OPERATION_INSERT, this::updateRow);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(request, deferred, true);
    }

    //TODO: Have not supported byte[] in params yet
    private void select(Message msg, Deferred<Message, Exception> deferred)  {
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var query = Jdbi.create(connectionPool::getConnection)
                .open()
                .createQuery(queryStatement);
        Helper.bindParams(query, msg.getPayload().getHeaders());
        ResultSet resultSet =  query.execute((supplier, context) -> supplier.get().executeQuery());
        List<Map<String, Object>> rows = Helper.resultSetAsList(resultSet);
        ack(deferred, rows, null);
    }

    private void updateRow(Message msg, Deferred<Message, Exception> deferred)  {
        var headers = msg.getPayload().getHeaders();
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var query = Jdbi.create(connectionPool::getConnection)
                .open()
                .createUpdate(queryStatement);
        Helper.bindParams(query, headers);
        int rowNum =  query.execute();
        ack(deferred, rowNum, null);
    }

    private void execute(Message msg, Deferred<Message, Exception> deferred){
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var handle = Jdbi.create(connectionPool::getConnection)
                .open();
        int result = handle.execute(queryStatement);
        ack(deferred, result, null);
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
