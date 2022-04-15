package io.arex.foundation.context;

import io.arex.foundation.util.StringUtil;
import io.arex.agent.bootstrap.TraceContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class);

    public static Map<String, ArexContext> RECORD_MAP = new ConcurrentHashMap<>();

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
            String replayId = TraceContextManager.get(createIfAbsent);
            TraceContextManager.set(caseId);
            return RECORD_MAP.computeIfAbsent(caseId, key -> ArexContext.of(key, replayId));
        }

        // record scene
        String traceId = TraceContextManager.get(createIfAbsent);
        if (StringUtil.isEmpty(traceId)) {
            return null;
        }

        return RECORD_MAP.computeIfAbsent(traceId, key -> ArexContext.of(key));
    }

    /**
     * agent will call this method
     */
    public static void remove() {
        String messageId = TraceContextManager.remove();
        if (StringUtil.isNotEmpty(messageId)) {
            RECORD_MAP.remove(messageId);
        }
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
}
