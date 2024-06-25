package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.cache.TimeCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Only used for ContextManager <br/>
 * delayed clean context in asynchronous situations,
 * the purpose is to ensure that the context can also be obtained during recording in async threads
 */
final class LatencyContextHashMap extends ConcurrentHashMap<String, ArexContext> {
    private static final long RECORD_TTL_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final ReentrantLock CLEANUP_LOCK = new ReentrantLock();

    @Override
    public ArexContext get(Object key) {
        if (key == null) {
            return null;
        }
        return super.get(key);
    }

    @Override
    public ArexContext remove(Object key) {
        if (key == null) {
            return null;
        }
        overdueCleanUp();

        return super.get(key);
    }

    private void overdueCleanUp() {
        if (CLEANUP_LOCK.tryLock()) {
            try {
                long now = System.currentTimeMillis();
                for (Map.Entry<String, ArexContext> entry: super.entrySet()) {
                    if (isExpired(entry.getValue().getCreateTime(), now)) {
                        // clear context attachments
                        entry.getValue().clear();
                        TimeCache.remove(entry.getKey());
                        super.remove(entry.getKey());
                    }
                }
            } finally {
                CLEANUP_LOCK.unlock();
            }
        }
    }

    private static boolean isExpired(long createTime, long now) {
        return now - createTime >= RECORD_TTL_MILLIS;
    }
}
