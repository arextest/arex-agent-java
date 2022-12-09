package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.DynamicClassMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.services.MockService;

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
        if (ContextManager.needReplay()) {
            Mocker mocker = createMocker();
            return UUID.fromString(String.valueOf(MockService.replayBody(mocker)));
        }
        UUID realUuid = UUID.randomUUID();
        if (ContextManager.needRecord()) {
            Mocker mocker = createMocker();
            mocker.getTargetResponse().setType(String.class.getName());
            mocker.getTargetResponse().setBody(realUuid.toString());
            MockService.recordMocker(mocker);
        }
        return realUuid;
    }

    private static Mocker createMocker() {
        Mocker mocker = MockService.createDynamicClass(UUID.class.getName(), "randomUUID");
        mocker.getTargetRequest().setBody(StringUtil.EMPTY);
        return mocker;
    }
}