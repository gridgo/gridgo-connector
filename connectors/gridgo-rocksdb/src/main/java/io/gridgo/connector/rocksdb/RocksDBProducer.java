package io.gridgo.connector.rocksdb;

import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION;
import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION_GET;
import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION_SET;

import java.util.HashMap;
import java.util.Map;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.util.SizeUnit;

import io.gridgo.bean.BElement;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocksDBProducer extends AbstractProducer {

    private Map<String, ProducerHandler> operations = new HashMap<>();

    private String path;

    private ConnectorConfig config;

    private Options options;

    private RocksDB db;

    public RocksDBProducer(ConnectorContext context, ConnectorConfig connectorConfig, String path) {
        super(context);
        this.config = connectorConfig;
        this.path = path;

        bindHandlers();
    }

    private void bindHandlers() {
        operations.put(OPERATION_SET, this::putValue);
        operations.put(OPERATION_GET, this::getValue);
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
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return _call(request, deferred, true);
    }

    private Promise<Message, Exception> _call(Message message, Deferred<Message, Exception> deferred, boolean isRPC) {
        var operation = message.getPayload().getHeaders().getString(OPERATION);
        var handler = operations.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(
                    new IllegalArgumentException("Operation " + operation + " is not supported"));
        }
        try {
            handler.handle(message, deferred, isRPC);
        } catch (RocksDBException e) {
            return new SimpleFailurePromise<>(e);
        }
        return deferred != null ? deferred.promise() : null;
    }

    private void putValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        // TODO check if we should only flush after batch writes
        var body = message.getPayload().getBody().asObject();
        for (var entry : body.entrySet()) {
            if (entry.getValue().asValue().isNull())
                db.delete(entry.getKey().getBytes());
            else
                db.put(entry.getKey().getBytes(), entry.getValue().asValue().toBytes());
        }
        ack(deferred, (Message) null);
    }

    private void getValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        var key = message.getPayload().getBody().asValue().getString().getBytes();
        var bytes = db.get(key);
        if (isRPC)
            ack(deferred, Message.ofAny(BElement.fromRaw(bytes)));
        else
            ack(deferred, (Message) null);
    }

    @Override
    protected void onStart() {
        try {
            this.options = createOptions();
            this.db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            log.error("Exception caught while starting RocksDB", e);
            getContext().getExceptionHandler().accept(e);
        }
    }

    @Override
    protected void onStop() {
        if (this.db != null)
            this.db.close();
        if (this.options != null)
            this.options.close();
    }

    private Options createOptions() {
        var options = new Options();
        options.setCreateIfMissing(getParamAsBoolean("createIfMissing", true)) //
               .setWriteBufferSize(getParamAsLong("writeBufferSize", 4 * SizeUnit.MB)) //
               .setAllow2pc(getParamAsBoolean("2pc", false)) //
               .setAllowMmapReads(getParamAsBoolean("mmapReads", false)) //
               .setAllowMmapWrites(getParamAsBoolean("mmapWrites", false));
        return options;
    }

    private long getParamAsLong(String name, long defaultValue) {
        Object value = config.getParameters().get(name);
        return value != null ? Long.parseLong(value.toString()) : defaultValue;
    }

    protected boolean getParamAsBoolean(String name, boolean defaultValue) {
        Object value = config.getParameters().get(name);
        return value != null ? Boolean.valueOf(value.toString()) : defaultValue;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    @Override
    protected String generateName() {
        return "consumer.rocksdb." + path;
    }

    interface ProducerHandler {

        public void handle(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) throws RocksDBException;
    }
}
