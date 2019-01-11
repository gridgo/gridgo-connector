package io.gridgo.connector.mysql;


import io.gridgo.connector.mysql.support.Helper;
import io.gridgo.framework.support.Message;
import org.jdbi.v3.core.Handle;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;


class Transaction {

    Transaction(Handle handle){
        this.handle = handle;
    }
    private final Handle handle;


    private Promise commit(Message msg){
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try (handle){
            handle.commit();
            Helper.ack(deferred, null, null);
        }
        return deferred;
    }

    private Promise rollback(Message msg){
        var deferred = new CompletableDeferredObject<Message, Exception>();
        try(handle){
            handle.rollback();
            Helper.ack(deferred, null, null);
        }
        return deferred;
    }

    public Promise select(Message msg){
        var deferred = new CompletableDeferredObject<Message, Exception>();
        MySQLOperator.select(msg, handle, deferred);
        return deferred;
    }

    public Promise update(Message msg){
        var deferred = new CompletableDeferredObject<Message, Exception>();
        MySQLOperator.updateRow(msg, handle, deferred);
        return deferred;
    }




}
