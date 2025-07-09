package io.arex.inst.runtime.listener;

public class EventSource {
    private final String caseId;
    private final String excludeMockTemplate;
    private final String recordInReplayMockTemplate;
    private static final EventSource EMPTY = new EventSource(null, null, null);

    private EventSource(String caseId, String excludeMockTemplate, String recordInReplayMockTemplate) {
        this.caseId = caseId;
        this.excludeMockTemplate = excludeMockTemplate;
        this.recordInReplayMockTemplate = recordInReplayMockTemplate;
    }

    public static EventSource of(String caseId, String excludeMockTemplate,String recordInReplayMockTemplate){
        return new EventSource(caseId, excludeMockTemplate,recordInReplayMockTemplate);
    }

    public static EventSource empty() {
        return EMPTY;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getExcludeMockTemplate() {
        return excludeMockTemplate;
    }

    public String getRecordInReplayMockTemplate() {
        return recordInReplayMockTemplate;
    }
}
