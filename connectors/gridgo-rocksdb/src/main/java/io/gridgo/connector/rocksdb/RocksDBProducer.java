package io.gridgo.connector.rocksdb;

import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_2_PHASE_COMMIT;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_MMAP_READS;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_ALLOW_MMAP_WRITES;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_CREATE_IF_MISSING;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_MAX_WRITE_BUFFER_NUMBER;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_MIN_WRITE_BUFFER_TO_MERGE;
import static io.gridgo.connector.rocksdb.RocksDBConstants.PARAM_WRITE_BUFFER_SIZE;

import org.joo.promise4j.Deferred;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.util.SizeUnit;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.keyvalue.AbstractKeyValueProducer;
import io.gridgo.connector.support.config.ConnectorConfig;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocksDBProducer extends AbstractKeyValueProducer {

    private ConnectorConfig config;

    private String path;

    private Options options;

    private RocksDB db;

    public RocksDBProducer(ConnectorContext context, ConnectorConfig connectorConfig, String path) {
        super(context);
        this.config = connectorConfig;
        this.path = path;
    }

    @Override
    public void putValue(Message message, BObject body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        if (!db.isOwningHandle()) {
            deferred.reject(new IllegalStateException("Handle is already closed"));
            return;
        }
        // TODO check if we should only flush after batch writes
        for (var entry : body.entrySet()) {
            var value = entry.getValue();
            if (value.isValue() && value.asValue().isNull())
                db.delete(entry.getKey().getBytes());
            else
                db.put(entry.getKey().getBytes(), value.toBytes());
        }
        ack(deferred, (Message) null);
    }

    @Override
    public void delete(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws Exception {
        if (!db.isOwningHandle()) {
            deferred.reject(new IllegalStateException("Handle is already closed"));
            return;
        }
        db.delete(body.getRaw());
        ack(deferred, (Message) null);
    }

    @Override
    public void getValue(Message message, BValue body, Deferred<Message, Exception> deferred, boolean isRPC)
            throws RocksDBException {
        if (!isRPC) {
            ack(deferred, (Message) null);
            return;
        }
        var key = body.getRaw();
        var bytes = db.get(key);
        if (bytes == null)
            ack(deferred);
        else
            ack(deferred, Message.ofAny(BElement.ofBytes(bytes)));
    }

    @Override
    public void getAll(Message message, Deferred<Message, Exception> deferred, boolean isRPC)
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
    protected String generateName() {
        return "consumer.rocksdb." + path;
    }
}
