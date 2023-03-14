package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import java.util.Random;
import java.util.UUID;

public class ReplaceMethodHelper {
    private static final String STRING_TYPE_NAME = String.class.getName();

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
            Mocker replayMocker = MockUtils.replayMocker(mocker);
            return UUID.fromString(replayMocker.getTargetResponse().getBody());
        }
        UUID realUuid = UUID.randomUUID();
        if (ContextManager.needRecord()) {
            Mocker mocker = createMocker();
            mocker.getTargetResponse().setType(STRING_TYPE_NAME);
            mocker.getTargetResponse().setBody(realUuid.toString());
            MockUtils.recordMocker(mocker);
        }
        return realUuid;
    }

    public static int nextInt(Object random, int bound) {
        if (ContextManager.needReplay()) {
            Mocker mocker = createNextIntMocker(bound);
            Mocker replayMocker = MockUtils.replayMocker(mocker);
            return Integer.parseInt(replayMocker.getTargetResponse().getBody());
        }
        int realNextInt = ((Random)random).nextInt(bound);
        try {
            if (ContextManager.needRecord()) {
                Mocker mocker = createNextIntMocker(bound);
                mocker.getTargetResponse().setBody(String.valueOf(realNextInt));
                mocker.getTargetResponse().setType(STRING_TYPE_NAME);
                MockUtils.recordMocker(mocker);
            }
        } catch (Throwable ex) {
            LogUtil.warn("replaceNextIntRecord", ex);
        }
        return realNextInt;
    }

    private static Mocker createMocker() {
        Mocker mocker = MockUtils.createDynamicClass(UUID.class.getName(), "randomUUID");
        mocker.getTargetRequest().setBody(StringUtil.EMPTY);
        return mocker;
    }

    private static Mocker createNextIntMocker(int bound) {
        Mocker mocker = MockUtils.createDynamicClass(Random.class.getName(), "nextInt");
        mocker.getTargetRequest().setBody(String.valueOf(bound));
        return mocker;
    }
}