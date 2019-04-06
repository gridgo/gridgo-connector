package io.gridgo.connector.keyvalue;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;

public interface ProducerHandler {

    public void handle(Message msg, Deferred<Message, Exception> deferred, boolean isRPC) throws Exception;
}
