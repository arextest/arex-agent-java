package io.arex.foundation.model;

public class DiffMocker {
    private String caseId;
    private String replayId;
    private String recordDiff;
    private String replayDiff;
    private MockerCategory category;

    public DiffMocker() {
    }

    public DiffMocker(MockerCategory category) {
        this.category = category;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
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

    public MockerCategory getCategory() {
        return category;
    }

    public void setCategory(MockerCategory category) {
        this.category = category;
    }
}
