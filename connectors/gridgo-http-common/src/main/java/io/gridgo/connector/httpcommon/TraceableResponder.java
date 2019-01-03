package io.gridgo.connector.httpcommon;

import org.joo.promise4j.Deferred;

import io.gridgo.connector.httpcommon.support.DeferredAndRoutingId;
import io.gridgo.framework.support.Message;

public interface TraceableResponder {

    public DeferredAndRoutingId registerTraceable();

    public void resolveTraceable(Message msg, Deferred<Message, Exception> deferredAck);
}
