package io.gridgo.connector.httpcommon;

import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BValue;
import io.gridgo.connector.httpcommon.support.DeferredAndRoutingId;
import io.gridgo.connector.impl.AbstractResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.generators.IdGenerator;
import io.gridgo.framework.support.generators.impl.AtomicIdGenerator;

public abstract class AbstractTraceableResponder extends AbstractResponder implements TraceableResponder {

    private final static IdGenerator ID_SEED = new AtomicIdGenerator();

    protected final Map<Object, Deferred<Message, Exception>> deferredResponses = new NonBlockingHashMap<>();

    protected AbstractTraceableResponder(ConnectorContext context) {
        super(context);
    }

    @Override
    public DeferredAndRoutingId registerTraceable() {
        var deferredResponse = new CompletableDeferredObject<Message, Exception>();
        var routingId = ID_SEED.generateId().orElseThrow().getData();
        this.deferredResponses.put(routingId, deferredResponse);
        deferredResponse.promise().always((stt, resp, ex) -> {
            deferredResponses.remove(routingId);
        });
        return DeferredAndRoutingId.builder() //
                                   .deferred(deferredResponse) //
                                   .routingId(BValue.of(routingId)) //
                                   .build();
    }

    @Override
    public void resolveTraceable(Message message, Deferred<Message, Exception> deferredAck) {
        try {
            message.getRoutingId().ifPresentOrElse(id -> {
                long routingId = id.getLong();
                var deferredResponse = this.deferredResponses.get(routingId);
                if (deferredResponse != null) {
                    deferredResponse.resolve(message);
                    this.ack(deferredAck);
                } else {
                    this.ack(deferredAck, new RuntimeException("Cannot find deferred for routing id: " + routingId));
                }
            }, () -> this.ack(deferredAck, new RuntimeException("Routing id must be provided")));
        } catch (Exception e) {
            deferredAck.reject(e);
        }
    }

    @Override
    protected void send(Message message, Deferred<Message, Exception> deferredAck) {
        resolveTraceable(message, deferredAck);
    }
}
