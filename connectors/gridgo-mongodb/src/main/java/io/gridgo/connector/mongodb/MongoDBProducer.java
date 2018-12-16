package io.gridgo.connector.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleFailurePromise;

import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Projections;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mongodb.support.MongoOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDBProducer extends AbstractProducer {

    private Map<String, ProducerHandler> operations = new HashMap<>();

    private MongoCollection<Document> collection;

    private MongoDatabase database;

    private String generatedName;

    public MongoDBProducer(ConnectorContext context, String connectionBean, String database, String collectionName) {
        super(context);
        var connection = getContext().getRegistry().lookupMandatory(connectionBean, MongoClient.class);
        this.database = connection.getDatabase(database);
        this.collection = this.database.getCollection(collectionName);
        this.generatedName = "producer.mongodb." + connectionBean + "." + database + "." + collectionName;

        bindHandlers();
    }

    private void bindHandlers() {
        bind(MongoDBConstants.OPERATION_INSERT, this::insertDocument);
        bind(MongoDBConstants.OPERATION_COUNT, this::countCollection);
        bind(MongoDBConstants.OPERATION_FIND_ALL, this::findAllDocuments);
        bind(MongoDBConstants.OPERATION_FIND_BY_ID, this::findById);
        bind(MongoDBConstants.OPERATION_UPDATE_ONE, this::updateDocument);
        bind(MongoDBConstants.OPERATION_UPDATE_MANY, this::updateManyDocuments);
        bind(MongoDBConstants.OPERATION_DELETE_ONE, this::deleteDocument);
        bind(MongoDBConstants.OPERATION_DELETE_MANY, this::deleteManyDocuments);
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

    private Promise<Message, Exception> _call(Message request, CompletableDeferredObject<Message, Exception> deferred,
            boolean isRPC) {
        var operation = request.getPayload().getHeaders().getString(MongoDBConstants.OPERATION);
        var handler = operations.get(operation);
        if (handler == null) {
            return new SimpleFailurePromise<>(
                    new IllegalArgumentException("Operation " + operation + " is not supported"));
        }
        try {
            handler.handle(request, deferred, isRPC);
        } catch (Exception ex) {
            log.error("Error while processing MongoDB request", ex);
            return new SimpleFailurePromise<>(ex);
        }
        return deferred != null ? deferred.promise() : null;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    public void bind(String name, ProducerHandler handler) {
        operations.put(name, handler);
    }

    public void insertDocument(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var body = msg.getPayload().getBody();
        if (body.isReference()) {
            var doc = convertToDocument(body.asReference());
            collection.insertOne(doc, (ignore, throwable) -> ack(deferred, null, throwable));
        } else {
            var docs = convertToDocuments(body.asArray());
            var options = getHeaderAs(msg, MongoDBConstants.INSERT_MANY_OPTIONS, InsertManyOptions.class);
            if (options != null)
                collection.insertMany(docs, options, (ignore, throwable) -> ack(deferred, null, throwable));
            else
                collection.insertMany(docs, (ignore, throwable) -> ack(deferred, null, throwable));
        }
    }

    public void updateDocument(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);

        var body = msg.getPayload().getBody();
        var doc = convertToDocument(body.asReference());
        collection.updateOne(filter, doc,
                (result, throwable) -> ack(deferred, isRPC ? result.getModifiedCount() : null, throwable));
    }

    public void updateManyDocuments(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);

        var body = msg.getPayload().getBody();
        var doc = convertToDocument(body.asReference());
        collection.updateMany(filter, doc,
                (result, throwable) -> ack(deferred, isRPC ? result.getModifiedCount() : null, throwable));
    }

    public void deleteDocument(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);
        collection.deleteOne(filter,
                (result, throwable) -> ack(deferred, isRPC ? result.getDeletedCount() : null, throwable));
    }

    public void deleteManyDocuments(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);
        collection.deleteMany(filter,
                (result, throwable) -> ack(deferred, isRPC ? result.getDeletedCount() : null, throwable));
    }

    public void findAllDocuments(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);

        var headers = msg.getPayload().getHeaders();
        int batchSize = headers.getInteger(MongoDBConstants.BATCH_SIZE, -1);
        int numToSkip = headers.getInteger(MongoDBConstants.NUM_TO_SKIP, -1);
        int limit = headers.getInteger(MongoDBConstants.LIMIT, -1);
        Bson sortBy = getHeaderAs(msg, MongoDBConstants.SORT_BY, Bson.class);

        var filterable = filter != null ? collection.find(filter) : collection.find();
        if (batchSize != -1)
            filterable.batchSize(batchSize);
        if (numToSkip != -1)
            filterable.skip(numToSkip);
        if (limit != -1)
            filterable.limit(limit);
        if (sortBy != null)
            filterable.sort(sortBy);
        applyProjection(msg, filterable);
        filterable.into(new ArrayList<>(), (result, throwable) -> {
            ack(deferred, isRPC ? result : null, throwable);
        });
    }

    private void applyProjection(Message msg, FindIterable<Document> filterable) {
        var headers = msg.getPayload().getHeaders();
        var project = getHeaderAs(msg, MongoDBConstants.PROJECT, Bson.class);
        var projectInclude = headers.getArray(MongoDBConstants.PROJECT_INCLUDE, null);
        var projectExclude = headers.getArray(MongoDBConstants.PROJECT_EXCLUDE, null);
        if (project != null || projectInclude != null || projectExclude != null) {
            if (projectInclude != null)
                project = Projections.include(toStringArray(projectInclude));
            else if (projectExclude != null)
                project = Projections.exclude(toStringArray(projectExclude));
            filterable.projection(project);
        }
    }

    private String[] toStringArray(BArray array) {
        return array.stream() //
                    .filter(element -> element.isValue()) //
                    .map(element -> element.asValue().getString()) //
                    .toArray(size -> new String[size]);
    }

    public void findById(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var headers = msg.getPayload().getHeaders();
        String idField = headers.getString(MongoDBConstants.ID_FIELD);
        Object id = msg.getPayload().getBody().asValue().getData();

        var filterable = collection.find(Filters.eq(idField, id));
        applyProjection(msg, filterable);
        filterable.first((result, throwable) -> ack(deferred, isRPC ? result : null, throwable));
    }

    public void countCollection(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);
        var options = getHeaderAs(msg, MongoDBConstants.COUNT_OPTIONS, CountOptions.class);
        if (options != null)
            collection.countDocuments(filter, options,
                    (result, throwable) -> ack(deferred, isRPC ? result : null, throwable));
        else
            collection.countDocuments(filter, (result, throwable) -> ack(deferred, isRPC ? result : null, throwable));
    }

    private void ack(Deferred<Message, Exception> deferred, Object result, Throwable throwable) {
        if (deferred == null)
            return;
        if (throwable != null) {
            if (throwable instanceof Exception)
                deferred.reject((Exception) throwable);
            else
                deferred.reject(new MongoOperationException(throwable));
        } else {
            deferred.resolve(convertToMessage(result));
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Message convertToMessage(Object result) {
        if (result == null)
            return null;
        if (result instanceof Long)
            return createMessage(BObject.ofEmpty(), BValue.of(result));
        if (result instanceof Document)
            return createMessage(BObject.ofEmpty(), toReference((Document) result));
        if (result instanceof List<?>) {
            var cloned = StreamSupport.stream(((List<Document>) result).spliterator(), false).map(this::toReference)
                                      .collect(Collectors.toList());
            return createMessage(BObject.ofEmpty(), BArray.of(cloned));
        }
        return null;
    }

    private BObject toReference(Document doc) {
        return BObject.of(doc);
    }

    private List<Document> convertToDocuments(BArray body) {
        return StreamSupport.stream(body.spliterator(), false).map(e -> convertToDocument(e.asReference()))
                            .collect(Collectors.toList());
    }

    private Document convertToDocument(BReference body) {
        return (Document) body.getReference();
    }

    private <T> T getHeaderAs(Message msg, String name, Class<T> clazz) {
        var options = msg.getPayload().getHeaders().get(name);
        if (options == null)
            return null;
        return clazz.cast(options.asReference().getReference());
    }

    @Override
    protected String generateName() {
        return generatedName;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    interface ProducerHandler {

        public void handle(Message msg, Deferred<Message, Exception> deferred, boolean isRPC);
    }
}
