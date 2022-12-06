package io.arex.foundation.model;

import io.arex.agent.bootstrap.model.MockCategoryType;

public class DiffMocker {
    private String recordId;
    private String replayId;
    private String recordDiff;
    private String replayDiff;
    private MockCategoryType categoryType;
    private int diffCount;

    public DiffMocker() {
    }

    public DiffMocker(MockCategoryType category) {
        this.categoryType = category;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getReplayId() {
        return replayId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public String getRecordDiff() {
        return recordDiff;
    }

    public void setRecordDiff(String recordDiff) {
        this.recordDiff = recordDiff;
    }

    public String getReplayDiff() {
        return replayDiff;
    }

    public void setReplayDiff(String replayDiff) {
        this.replayDiff = replayDiff;
    }

    public MockCategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(MockCategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public int getDiffCount() {
        return diffCount;
    }

    public void setDiffCount(int diffCount) {
        this.diffCount = diffCount;
    }
}