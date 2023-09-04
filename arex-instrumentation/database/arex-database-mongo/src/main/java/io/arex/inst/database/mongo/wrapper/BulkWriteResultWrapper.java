package io.arex.inst.database.mongo.wrapper;

import com.mongodb.bulk.BulkWriteUpsert;
import java.util.List;

public class BulkWriteResultWrapper {
    private int insertedCount;
    private boolean acknowledged;
    private int matchedCount;
    private int deletedCount;

    private boolean modifiedCountAvailable;
    private Integer modifiedCount;
    private List<BulkWriteUpsert> upserts;

    public BulkWriteResultWrapper() {
    }

    public BulkWriteResultWrapper(int insertedCount, boolean acknowledged, int matchedCount, int deletedCount, boolean modifiedCountAvailable, Integer modifiedCount, List<BulkWriteUpsert> upserts) {
        this.insertedCount = insertedCount;
        this.acknowledged = acknowledged;
        this.matchedCount = matchedCount;
        this.deletedCount = deletedCount;
        this.modifiedCountAvailable = modifiedCountAvailable;
        this.modifiedCount = modifiedCount;
        this.upserts = upserts;
    }

    public BulkWriteResultWrapper(boolean wasAcknowledged) {
        this.acknowledged = wasAcknowledged;
    }


    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public boolean isModifiedCountAvailable() {
        return modifiedCountAvailable;
    }

    public void setModifiedCountAvailable(boolean modifiedCountAvailable) {
        this.modifiedCountAvailable = modifiedCountAvailable;
    }

    public Integer getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(Integer modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public List<BulkWriteUpsert> getUpserts() {
        return upserts;
    }

    public void setUpserts(List<BulkWriteUpsert> upserts) {
        this.upserts = upserts;
    }
}
