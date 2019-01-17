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

import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Projections;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.mongodb.support.MongoOperationException;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDBProducer extends AbstractProducer {

    interface ProducerHandler {

        public void handle(Message msg, Deferred<Message, Exception> deferred, boolean isRPC);
    }

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

    public void bind(String name, ProducerHandler handler) {
        operations.put(name, handler);
    }

    private Promise<Message, Exception> doCall(Message request, CompletableDeferredObject<Message, Exception> deferred,
            boolean isRPC) {
        var operation = request.headers().getString(MongoDBConstants.OPERATION);
        var handler = operations.get(operation);
        if (handler == null) {
            return Promise.ofCause(new IllegalArgumentException("Operation " + operation + " is not supported"));
        }
        try {
            handler.handle(request, deferred, isRPC);
        } catch (Exception ex) {
            log.error("Error while processing MongoDB request", ex);
            return Promise.ofCause(ex);
        }
        return deferred != null ? deferred.promise() : null;
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return doCall(request, deferred, true);
    }

    public void countCollection(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);
        var options = getHeaderAs(msg, MongoDBConstants.COUNT_OPTIONS, CountOptions.class);
        if (options == null)
            options = new CountOptions();
        collection.countDocuments(filter, options,
                (result, throwable) -> ack(deferred, isRPC ? result : null, throwable));
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

        var headers = msg.headers();
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

    public void findById(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var headers = msg.headers();
        String idField = headers.getString(MongoDBConstants.ID_FIELD);
        Object id = msg.body().asValue().getData();

        var filterable = collection.find(Filters.eq(idField, id));
        applyProjection(msg, filterable);
        filterable.first((result, throwable) -> ack(deferred, isRPC ? result : null, throwable));
    }

    private void applyProjection(Message msg, FindIterable<Document> filterable) {
        var headers = msg.headers();
        var project = getHeaderAs(msg, MongoDBConstants.PROJECT, Bson.class);
        var projectInclude = headers.getArray(MongoDBConstants.PROJECT_INCLUDE, null);
        var projectExclude = headers.getArray(MongoDBConstants.PROJECT_EXCLUDE, null);
        if (project != null || projectInclude != null || projectExclude != null) {
            project = getProject(project, projectInclude, projectExclude);
            filterable.projection(project);
        }
    }

    private Bson getProject(Bson project, BArray projectInclude, BArray projectExclude) {
        if (projectInclude != null)
            return Projections.include(toStringArray(projectInclude));
        if (projectExclude != null)
            return Projections.exclude(toStringArray(projectExclude));
        return project;
    }

    private String[] toStringArray(BArray array) {
        return array.stream() //
                    .filter(element -> element.isValue()) //
                    .map(element -> element.asValue().getString()) //
                    .toArray(size -> new String[size]);
    }

    public void insertDocument(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var body = msg.body();
        if (body.isReference()) {
            insertSingleDocument(deferred, body);
        } else {
            insertManyDocuments(msg, deferred, body);
        }
    }

    private void insertManyDocuments(Message msg, Deferred<Message, Exception> deferred, BElement body) {
        var docs = convertToDocuments(body.asArray());
        var options = getHeaderAs(msg, MongoDBConstants.INSERT_MANY_OPTIONS, InsertManyOptions.class);
        if (options == null)
            options = new InsertManyOptions();
        collection.insertMany(docs, options, (ignore, throwable) -> ack(deferred, null, throwable));
    }

    private void insertSingleDocument(Deferred<Message, Exception> deferred, BElement body) {
        var doc = convertToDocument(body);
        collection.insertOne(doc, (ignore, throwable) -> ack(deferred, null, throwable));
    }

    public void updateDocument(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);

        var doc = convertToDocument(msg.body());
        collection.updateOne(filter, doc,
                (result, throwable) -> ack(deferred, isRPC ? result.getModifiedCount() : null, throwable));
    }

    public void updateManyDocuments(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) {
        var filter = getHeaderAs(msg, MongoDBConstants.FILTER, Bson.class);

        var doc = convertToDocument(msg.body());
        collection.updateMany(filter, doc,
                (result, throwable) -> ack(deferred, isRPC ? result.getModifiedCount() : null, throwable));
    }

    private <T> T getHeaderAs(Message msg, String name, Class<T> clazz) {
        var options = msg.headers().get(name);
        if (options == null || options.isNullValue())
            return null;
        return clazz.cast(options.asReference().getReference());
    }

    private void ack(Deferred<Message, Exception> deferred, Object result, Throwable throwable) {
        if (throwable != null) {
            ack(deferred, convertToException(throwable));
        } else {
            ack(deferred, convertToMessage(result));
        }
    }

    private Exception convertToException(Throwable throwable) {
        if (throwable instanceof Exception)
            return (Exception) throwable;
        return new MongoOperationException(throwable);
    }

    @SuppressWarnings({ "unchecked" })
    private Message convertToMessage(Object result) {
        if (result == null)
            return null;
        if (result instanceof Long)
            return createMessage(BObject.ofEmpty(), BValue.of(result));
        if (result instanceof Document)
            return createMessage(BObject.ofEmpty(), toBElement((Document) result));
        if (result instanceof List<?>) {
            var cloned = StreamSupport.stream(((List<Document>) result).spliterator(), false) //
                                      .map(this::toBElement) //
                                      .collect(Collectors.toList());
            return createMessage(BObject.ofEmpty(), BArray.of(cloned));
        }
        return null;
    }

    private BObject toBElement(Document doc) {
        return BObject.of(doc);
    }

    private List<Document> convertToDocuments(BArray body) {
        return StreamSupport.stream(body.spliterator(), false) //
                            .map(this::convertToDocument) //
                            .collect(Collectors.toList());
    }

    private Document convertToDocument(BElement body) {
        return body.asReference().getReference();
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
        // Nothing to do here
    }

    @Override
    public void send(Message message) {
        doCall(message, null, false);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        return doCall(message, deferred, false);
    }

    @Override
    protected String generateName() {
        return generatedName;
    }
}
