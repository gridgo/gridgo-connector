package io.gridgo.connector.jdbc;

import static io.gridgo.connector.jdbc.JdbcConstants.OPERATION;
import static io.gridgo.connector.support.transaction.TransactionConstants.HEADER_CREATE_TRANSACTION;

import io.gridgo.connector.jdbc.support.Helper;
import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;

import io.gridgo.connector.jdbc.support.JdbcOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JdbcProducer extends JdbcClient {

    private Jdbi jdbiClient;

    JdbcProducer(ConnectorContext context, ConnectionFactory connectionFactory) {
        super(context);
        this.jdbiClient = Jdbi.create(connectionFactory);
    }

    private Message beginTransaction(Handle handle, ConnectorContext context) {
        handle.begin();
        JdbcTransaction jdbcTransaction = new JdbcTransaction(handle, context);
        return Message.ofAny(jdbcTransaction);
    }

    protected Promise<Message, Exception> doCall(Message request,
            CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        var operationParam = request.headers().remove(OPERATION);
        var sqlStatement = request.body().asValue().getString();
        String operation = operationParam == null ? Helper.getOperation(sqlStatement) : operationParam.toString();
        if (HEADER_CREATE_TRANSACTION.equals(operation)) {
            var result = beginTransaction(jdbiClient.open(), this.getContext());
            ack(deferred, result);
            return deferred == null ? null : deferred.promise();
        }
        var handler = operationsMap.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(new JdbcOperationException());
        }
        try (var handle = jdbiClient.open()) {
            var result = handler.handle(request, handle);
            ack(deferred, result);
        } catch (Exception ex) {
            log.error("Error while processing JDBC request", ex);
            ack(deferred, ex);
        }
        return deferred == null ? null : deferred.promise();
    }

    @Override
    protected String generateName() {
        return "producer.jdbc";
    }
}
