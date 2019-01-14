package io.gridgo.connector.support.transaction;

import io.gridgo.connector.support.MessageSenderComponent;

/**
 * Represent an asychronous transaction. It can be used to call requests inside
 * a transaction boundary, commit the transaction or rollback it.
 */
public interface Transaction extends MessageSenderComponent {

    /**
     * Commit the transaction and close the underlying connection. This method
     * should be idempotent, i.e calling multiple times has the same effect as
     * calling once.
     * 
     * @return the transaction itself
     */
    public Transaction commit();

    /**
     * Rollback the transaction and close the underlying connection. This method
     * should be idempotent, i.e calling multiple times has the same effect as
     * calling once. Calling this method after {@link #commit()} is called has no
     * effect.
     * 
     * @return the transaction itself
     */
    public Transaction rollback();
}
