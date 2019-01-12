package io.gridgo.connector.support.transaction;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTransaction implements Transaction {

    private AtomicBoolean finished = new AtomicBoolean();

    @Override
    public Transaction commit() {
        if (finished.compareAndSet(false, true))
            doCommit();
        return this;
    }

    @Override
    public Transaction rollback() {
        if (finished.compareAndSet(false, true))
            doRollback();
        return this;
    }

    protected abstract void doCommit();

    protected abstract void doRollback();
}
