package io.gridgo.connector.mysql;


import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.connector.mysql.support.MySQLOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class MySQLProducer extends AbstractProducer {

    interface ProducerHandler {

        public void handle(Message msg, Deferred<Message, Exception> deferred, boolean isRPC);
    }

    private Map<String, ProducerHandler> operations = new HashMap<>();

    private ConnectionPool connectionPool;


    public MySQLProducer(ConnectorContext context, String url, String userName, String password) {
        super(context);
        this.connectionPool = new ConnectionPool("local", 5, 10, 30, 180, url, userName, password);
        bindHandlers();
    }

    private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        var operation = String.valueOf(request.getPayload().getHeaders().remove(MySQLConstants.OPERATION));
        if(deferred == null){
            return null;
        }
        var handler = operations.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(new IllegalArgumentException("Operation " + operation + " is not supported"));
        }
        try {
            handler.handle(request, deferred, isRPC);
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
        deferred.resolve(convertToMessage(result));
    }


    public void bind(String name, ProducerHandler handler) {
        operations.put(name, handler);
    }

    private void bindHandlers() {
        bind(MySQLConstants.OPERATION_SELECT, this::select);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(request, deferred, true);
    }


    private void select(Message msg, Deferred<Message, Exception> deferred, boolean isRPC)  {
        var headers = msg.getPayload().getHeaders();
        var params = headers.keySet();
        var queryStatement = msg.getPayload().getBody().asValue().getString();
        var query = Jdbi.create(connectionPool::getConnection)
                .open()
                .createQuery(queryStatement);
        for (String param: params) {
            query.bind(param, headers.getRaw(param));
        }
        ResultSet resultSet =  query.execute((supplier, context) -> supplier.get().executeQuery());
        List<Map<String, Object>> rows = Helper.resultSetAsList(resultSet);
         ack(deferred, isRPC ? rows : null, null);
    }

    @SuppressWarnings({ "unchecked" })
    private Message convertToMessage(Object result) {
        if (result == null)
            return null;
        if (result instanceof List<?>) {
            var cloned = StreamSupport.stream(((List<HashMap<String, Object>>) result).spliterator(), false).collect(Collectors.toList());
            return createMessage(BObject.ofEmpty(), BArray.of(cloned));
        }
        return null;
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
