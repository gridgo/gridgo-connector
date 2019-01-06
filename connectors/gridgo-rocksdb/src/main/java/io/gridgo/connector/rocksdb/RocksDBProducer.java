package io.gridgo.connector.rocksdb;

import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION;
import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION_GET;
import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION_GET_ALL;
import static io.gridgo.connector.rocksdb.RocksDBConstants.OPERATION_SET;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_2_PHASE_COMMIT;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_MMAP_READS;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_MMAP_WRITES;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_CREATE_IF_MISSING;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_MAX_WRITE_BUFFER_NUMBER;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_MIN_WRITE_BUFFER_TO_MERGE;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_WRITE_BUFFER_SIZE;

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
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.impl.DefaultExecutionStrategy;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocksDBProducer extends AbstractProducer {

    private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = new DefaultExecutionStrategy();

    private Map<String, ProducerHandler> operations = new HashMap<>();

    private ConnectorConfig config;

    private String path;

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
        operations.put(OPERATION_GET_ALL, this::getAllValues);
    }

    @Override
    public void send(Message message) {
        _call(message, false, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return _call(message, true, false);
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        return _call(request, true, true);
    }

    private Promise<Message, Exception> _call(Message message, boolean deferredRequired, boolean isRPC) {
        // get the operation and associated handler
        var operation = message.headers().getString(OPERATION);
        var handler = operations.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(
                    new IllegalArgumentException("Operation " + operation + " is not supported"));
        }

        // call the handler with deferred if required
        var deferred = deferredRequired ? new CompletableDeferredObject<Message, Exception>() : null;
        var strategy = getContext().getProducerExecutionStrategy().orElse(DEFAULT_EXECUTION_STRATEGY);

        strategy.execute(() -> {
            try {
                handler.handle(message, deferred, isRPC);
            } catch (RocksDBException ex) {
                log.error("Exception caught while executing handler", ex);
                deferred.reject(ex);
            }
        });
        return deferred != null ? deferred.promise() : null;
    }

    private void putValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        // TODO check if we should only flush after batch writes
        var body = message.body().asObject();
        for (var entry : body.entrySet()) {
            var value = entry.getValue();
            if (value.isValue() && value.asValue().isNull())
                db.delete(entry.getKey().getBytes());
            else
                db.put(entry.getKey().getBytes(), value.toBytes());
        }
        ack(deferred, (Message) null);
    }

    private void getValue(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        if (!isRPC) {
            ack(deferred, (Message) null);
            return;
        }
        var key = message.body().asValue().getString().getBytes();
        var bytes = db.get(key);
        ack(deferred, Message.ofAny(BElement.ofBytes(bytes)));
    }

    private void getAllValues(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        if (!isRPC) {
            ack(deferred, (Message) null);
            return;
        }
        var body = BObject.ofEmpty();
        try (var iterator = db.newIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                body.setAny(new String(iterator.key()), BElement.ofBytes(iterator.value()));
                iterator.next();
            }
            ack(deferred, Message.ofAny(body));
        }
    }

    @Override
    protected void onStart() {
        try {
            createOptions();
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

    private void createOptions() {
        this.options = new Options();
        this.options.setCreateIfMissing(getParamAsBoolean(PARAM_CREATE_IF_MISSING, true)) //
                    .setWriteBufferSize(getParamAsLong(PARAM_WRITE_BUFFER_SIZE, 4 * SizeUnit.MB)) //
                    .setMaxWriteBufferNumber(getParamAsInt(PARAM_MAX_WRITE_BUFFER_NUMBER, 2)) //
                    .setMinWriteBufferNumberToMerge(getParamAsInt(PARAM_MIN_WRITE_BUFFER_TO_MERGE, 1)) //
                    .setAllow2pc(getParamAsBoolean(PARAM_ALLOW_2_PHASE_COMMIT, false)) //
                    .setAllowMmapReads(getParamAsBoolean(PARAM_ALLOW_MMAP_READS, false)) //
                    .setAllowMmapWrites(getParamAsBoolean(PARAM_ALLOW_MMAP_WRITES, false));
    }

    private long getParamAsLong(String name, long defaultValue) {
        Object value = config.getParameters().get(name);
        return value != null ? Long.parseLong(value.toString()) : defaultValue;
    }

    private int getParamAsInt(String name, int defaultValue) {
        Object value = config.getParameters().get(name);
        return value != null ? Integer.parseInt(value.toString()) : defaultValue;
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
