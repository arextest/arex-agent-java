package io.arex.inst.runtime.listener;

public class EventSource {
    private final String caseId;
    private final String excludeMockTemplate;
    private static final EventSource EMPTY = new EventSource(null, null);

    private EventSource(String caseId, String excludeMockTemplate) {
        this.caseId = caseId;
        this.excludeMockTemplate = excludeMockTemplate;
    }

    public static EventSource of(String caseId, String excludeMockTemplate){
        return new EventSource(caseId, excludeMockTemplate);
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
}
