package io.gridgo.connector.mysql;


import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mysql.support.JdbcOperationException;
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

import java.util.HashMap;
import java.util.Map;

import static io.gridgo.connector.mysql.JdbcConstants.*;

@Slf4j
class JdbcProducer extends JdbcClient {


    private Jdbi jdbiClient;
    private String generatedName;

    JdbcProducer(ConnectorContext context, ConnectionFactory connectionFactory) {
        super(context);
        this.jdbiClient = Jdbi.create(connectionFactory);
        this.generatedName = "producer.jdbc";
    }

    private Message beginTransaction(Message msg, Handle handle, ConnectorContext context) {
        handle.begin();
        JdbcTransaction jdbcTransaction = new JdbcTransaction(handle, context);
        return Message.ofAny(jdbcTransaction);
    }

    protected Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred, boolean isRPC) {
        if (deferred == null) {
            return null;
        }
        var operation = request.headers().getString(OPERATION);
        if (BEGIN_TRANSACTION.equals(operation)) {
            var result = beginTransaction(request, jdbiClient.open(), this.getContext());
            ack(deferred, result);
            return deferred.promise();
        }
        var handler = operationsMap.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(new JdbcOperationException());
        }
        try (Handle handle = jdbiClient.open()) {
            Message result = handler.handle(request, handle);
            ack(deferred, result);
        } catch (Exception ex) {
            log.error("Error while processing JDBC request", ex);
            ack(deferred, ex);
        }
        return deferred.promise();
    }

    @Override
    protected String generateName() {
        return this.generatedName;
    }






}
