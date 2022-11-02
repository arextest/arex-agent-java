package io.arex.foundation.context;

import io.arex.foundation.util.StringUtil;
import io.arex.agent.bootstrap.TraceContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.arex.foundation.util.LogUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ContextManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class);

    public static Map<String, ArexContext> RECORD_MAP = new ConcurrentHashMap<>();
    private static final long RECORD_TTL_MILLIS = TimeUnit.MINUTES.toMillis(2);
    private static final ReentrantLock CLEANUP_LOCK = new ReentrantLock();

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
            return RECORD_MAP.put(caseId, ArexContext.of(caseId, replayId));
        }

        // record scene
        String traceId = TraceContextManager.get(createIfAbsent);
        if (StringUtil.isEmpty(traceId)) {
            return null;
        }

        return RECORD_MAP.computeIfAbsent(traceId, key -> ArexContext.of(key));
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

    public static void overdueCleanUp() {
        if (CLEANUP_LOCK.tryLock()) {
            List<String> removeRecordIds = new ArrayList<>(RECORD_MAP.size());
            try {
                long now = System.currentTimeMillis();
                for (Map.Entry<String, ArexContext> entry: RECORD_MAP.entrySet()) {
                    if (isExpired(entry.getValue().getCreateTime(), now)) {
                        entry.getValue().clear();
                        RECORD_MAP.remove(entry.getKey());
                        removeRecordIds.add(entry.getKey());
                    }
                }
            } finally {
                CLEANUP_LOCK.unlock();
                if (removeRecordIds.size() > 0) {
                    LOGGER.info("{}clean up expired count: {}, arex-record-id: {}", LogUtil.buildTitle("cleanUp"),
                            removeRecordIds.size(), String.join(",", removeRecordIds));
                }
            }
        }
    }

    private static boolean isExpired(long createTime, long now) {
        return now - createTime >= RECORD_TTL_MILLIS;
    }
}
