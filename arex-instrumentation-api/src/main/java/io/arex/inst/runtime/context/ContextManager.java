package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.listener.ContextListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContextManager {
    private static final Map<String, ArexContext> RECORD_MAP = new LatencyContextHashMap();
    private static final List<ContextListener> LISTENERS = new ArrayList<>();

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
            publish(context, true);
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
            ArexContext context = ArexContext.of(caseId);
            publish(context, true);
            return RECORD_MAP.put(caseId, context);
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
        ArexContext context = RECORD_MAP.remove(caseId);
        publish(context, false);
    }

    public static void registerListener(ContextListener listener) {
        LISTENERS.add(listener);
    }

    private static void publish(ArexContext context, boolean isCreate) {
        if (LISTENERS.size() > 0) {
            LISTENERS.stream().forEach(listener -> {
                if (isCreate) {
                    listener.onCreate(context);
                } else {
                    listener.onComplete(context);
                }
            });
        }
    }
}
