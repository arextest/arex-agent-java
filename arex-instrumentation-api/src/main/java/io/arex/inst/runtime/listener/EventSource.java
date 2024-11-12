package io.arex.inst.runtime.listener;

public class EventSource {
    private final String caseId;
    private final String excludeMockTemplate;
    private final Object source;
    private static final EventSource EMPTY = new EventSource(null, null, null);

    private EventSource(String caseId, String excludeMockTemplate, Object source) {
        this.caseId = caseId;
        this.excludeMockTemplate = excludeMockTemplate;
        this.source = source;
    }

    public static EventSource of(String caseId, String excludeMockTemplate){
        return new EventSource(caseId, excludeMockTemplate, null);
    }

    public static EventSource of(Object source){
        return new EventSource(null, null, source);
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

    public Object getSource() {
        return source;
    }
}
