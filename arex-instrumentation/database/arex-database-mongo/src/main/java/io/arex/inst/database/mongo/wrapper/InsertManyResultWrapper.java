package io.arex.inst.database.mongo.wrapper;

public class InsertManyResultWrapper<T> {
    T insertedIds;
    boolean acknowledged;

    public InsertManyResultWrapper() {
    }

    public InsertManyResultWrapper(T insertedIds, boolean wasAcknowledged) {
        this.insertedIds = insertedIds;
        this.acknowledged = wasAcknowledged;
    }

    public T getInsertedIds() {
        return insertedIds;
    }

    public void setInsertedIds(T insertedIds) {
        this.insertedIds = insertedIds;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
