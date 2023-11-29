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
     * record scene: recordId is map key
     * replay scene: replayId is map key
     */
    public static ArexContext currentContext(boolean createIfAbsent, String recordId) {
        String traceId = TraceContextManager.get(createIfAbsent);
        if (StringUtil.isEmpty(traceId)) {
            return null;
        }
        if (createIfAbsent) {
            final ArexContext arexContext = createContext(recordId, traceId);
            publish(arexContext, true);
            RECORD_MAP.put(traceId, arexContext);
            return arexContext;
        }
        return RECORD_MAP.get(traceId);
    }

    /**
     *  ArexContext.of(recordId, replayId)
     */
    private static ArexContext createContext(String recordId, String traceId) {
        // replay scene: traceId is replayId
        if (StringUtil.isNotEmpty(recordId)) {
            return ArexContext.of(recordId, traceId);
        }
        // record scene: traceId is recordId
        return ArexContext.of(traceId);
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
