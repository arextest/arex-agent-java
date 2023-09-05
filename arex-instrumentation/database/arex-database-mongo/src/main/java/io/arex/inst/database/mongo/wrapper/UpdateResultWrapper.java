package io.arex.inst.database.mongo.wrapper;

import org.bson.BsonValue;

public class UpdateResultWrapper {
    private long matchedCount;
    private long modifiedCount;
    private boolean acknowledged;
    private BsonValue upsertedId;

    public UpdateResultWrapper() {
    }

    public UpdateResultWrapper(long matchedCount, long modifiedCount, boolean acknowledged, BsonValue upsertedId) {
        this.matchedCount = matchedCount;
        this.modifiedCount = modifiedCount;
        this.acknowledged = acknowledged;
        this.upsertedId = upsertedId;
    }

    public long getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(long matchedCount) {
        this.matchedCount = matchedCount;
    }

    public long getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(long modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public BsonValue getUpsertedId() {
        return upsertedId;
    }

    public void setUpsertedId(BsonValue upsertedId) {
        this.upsertedId = upsertedId;
    }


}
