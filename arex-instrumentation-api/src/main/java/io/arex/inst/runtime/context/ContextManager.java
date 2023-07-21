package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.StringUtil;

import java.util.Map;

public class ContextManager {
    private static final Map<String, ArexContext> RECORD_MAP = new LatencyContextHashMap();

    /**
     * agent call this method
     */
    public static ArexContext currentContext() {
        return currentContext(false, null);
    }

    /**
     * agent will call this method
     */
    public static ArexContext currentContext(boolean createIfAbsent, String caseId) {
        // replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            TraceContextManager.set(caseId);
            ArexContext context = ArexContext.of(caseId, TraceContextManager.generateId());
            // Each replay init generates the latest context(maybe exist previous recorded context)
            RECORD_MAP.put(caseId, context);
            return context;
        }

        // record scene
        caseId = TraceContextManager.get(createIfAbsent);
        if (StringUtil.isEmpty(caseId)) {
            return null;
        }
        // first init execute
        if (createIfAbsent) {
            return RECORD_MAP.computeIfAbsent(caseId, ArexContext::of);
        }
        return RECORD_MAP.get(caseId);
    }

    public static ArexContext getRecordContext(String recordId) {
        return RECORD_MAP.get(recordId);
    }

    public static boolean needRecord() {
        ArexContext context = currentContext();
        return context != null && !context.isReplay();
    }

    public static boolean needReplay() {
        ArexContext context = currentContext();
        return context != null && context.isReplay();
    }

    public static boolean needRecordOrReplay() {
        return currentContext() != null;
    }

    public static void remove() {
        String caseId = TraceContextManager.remove();
        if (StringUtil.isEmpty(caseId)) {
            return;
        }
        RECORD_MAP.remove(caseId);
    }
}
