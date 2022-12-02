package io.arex.foundation.listener;

public class EventSource {
    private String caseId;
    private String excludeMockTemplate;

    private EventSource(String caseId, String excludeMockTemplate) {
        this.caseId = caseId;
        this.excludeMockTemplate = excludeMockTemplate;
    }

    public static EventSource of(String caseId, String excludeMockTemplate){
        return new EventSource(caseId, excludeMockTemplate);
    }

    public String getCaseId() {
        return caseId;
    }

    public String getExcludeMockTemplate() {
        return excludeMockTemplate;
    }
}
