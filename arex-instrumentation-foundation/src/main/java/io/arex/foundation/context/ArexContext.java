package io.arex.foundation.context;


import io.arex.foundation.util.StringUtil;

public class ArexContext {
    private final String caseId;
    private final String replayId;
    private final long createTime;
    private final SequenceProvider sequence;

    public String getCaseId() {
        return this.caseId;
    }

    public String getReplayId() {
        return this.replayId;
    }

    public long getCreateTime() {
        return createTime;
    }

    private ArexContext(String caseId, String replayId) {
        this.createTime = System.currentTimeMillis();
        this.caseId = caseId;
        this.sequence = new SequenceProvider();
        this.replayId = replayId;
    }

    public boolean isReplay() {
        return StringUtil.isNotEmpty(this.replayId);
    }

    public void add(String key, String value) {

    }

    public String get(String key) {
        return null;
    }

    public int calculateSequence(String target) {
        return StringUtil.isEmpty(target) ? 0 : sequence.get(target);
    }

    public int calculateSequence() {
        return 0;
    }

    public static ArexContext of(String caseId) {
        return of(caseId, null);
    }

    public static ArexContext of(String caseId, String replayId) {
        return new ArexContext(caseId, replayId);
    }
}
