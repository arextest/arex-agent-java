package io.arex.inst.database.mongo.wrapper;

public class InsertOneResultWrapper<T> {
    T insertedId;
    boolean acknowledged;

    public InsertOneResultWrapper() {
    }

    public InsertOneResultWrapper(T insertedId, boolean wasAcknowledged) {
        this.insertedId = insertedId;
        this.acknowledged = wasAcknowledged;
    }

    public T getInsertedId() {
        return insertedId;
    }

    public void setInsertedId(T insertedId) {
        this.insertedId = insertedId;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setWasAcknowledged(boolean wasAcknowledged) {
        this.acknowledged = wasAcknowledged;
    }
}
