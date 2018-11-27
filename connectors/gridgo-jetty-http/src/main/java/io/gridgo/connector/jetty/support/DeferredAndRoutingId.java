package io.gridgo.connector.jetty.support;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeferredAndRoutingId {

	private Deferred<Message, Exception> deferred;

	private long routingId;
}
