package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.DynamicClassMocker;
import java.util.UUID;

public class ReplaceMethodHelper {

    public static long currentTimeMillis() {
        if (ContextManager.needReplay()) {
            long mockTimeMillis = TimeCache.get();
            if (mockTimeMillis > 0L) {
                return mockTimeMillis;
            }
        }

        return System.currentTimeMillis();
    }

    public static UUID uuid() {
        UUID realUuid = UUID.randomUUID();

        if (ContextManager.needRecord()) {
            DynamicClassMocker mocker = new DynamicClassMocker("java.util.UUID", "randomUUID", "", realUuid.toString(), "java.lang.String");
            mocker.record();
        }

        if (ContextManager.needReplay()) {
            DynamicClassMocker mocker = new DynamicClassMocker("java.util.UUID", "randomUUID", "");
            return UUID.fromString(String.valueOf(mocker.replay()));
        }

        return realUuid;
    }
}
