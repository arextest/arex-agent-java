package io.arex.inst.database.mongo.wrapper;

public class DeleteResultWrapper {
    private long deletedCount;
    private boolean acknowledged;

    public DeleteResultWrapper() {
    }

    public DeleteResultWrapper(long deletedCount, boolean acknowledged) {
        this.deletedCount = deletedCount;
        this.acknowledged = acknowledged;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
