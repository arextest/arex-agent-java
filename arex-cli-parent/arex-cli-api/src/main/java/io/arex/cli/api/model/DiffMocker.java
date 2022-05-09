package io.arex.cli.api.model;

public class DiffMocker {
    private String caseId;
    private String replayId;
    private String recordDiff;
    private String replayDiff;
    private String category;
    private int diffCount;

    public DiffMocker() {
    }

    public DiffMocker(String category) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDiffCount() {
        return diffCount;
    }

    public void setDiffCount(int diffCount) {
        this.diffCount = diffCount;
    }
}
