package io.arex.inst.dynamic;

import static io.arex.inst.runtime.model.ArexConstants.*;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class ReplaceMethodHelper {
    private static final String STRING_TYPE_NAME = String.class.getName();
    private static final List<String> REPLACE_METHODS_SIGNATURE = CollectionUtil.newArrayList(
            UUID_SIGNATURE,CURRENT_TIME_MILLIS_SIGNATURE,NEXT_INT_SIGNATURE);
    public static boolean needReplace(DynamicClassEntity entity) {
        return REPLACE_METHODS_SIGNATURE.contains(entity.getAdditionalSignature());
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

    private static boolean replayResultIsNull(Mocker replayMocker) {
        return replayMocker == null ||
                replayMocker.getTargetResponse() == null ||
                StringUtil.isEmpty(replayMocker.getTargetResponse().getBody());
    }

    // region replace method
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
        if (ContextManager.needReplay()) {
            Mocker mocker = createMocker();
            Mocker replayMocker = MockUtils.replayMocker(mocker);
            if (replayResultIsNull(replayMocker)) {
                return realUuid;
            }
            return UUID.fromString(replayMocker.getTargetResponse().getBody());
        }
        try {
            if (ContextManager.needRecord()) {
                Mocker mocker = createMocker();
                mocker.getTargetResponse().setType(STRING_TYPE_NAME);
                mocker.getTargetResponse().setBody(realUuid.toString());
                MockUtils.recordMocker(mocker);
            }
        } catch (Throwable ex) {
            LogUtil.warn("replaceUuidRecord", ex);
        }
        return realUuid;
    }

    public static int nextInt(Object random, int bound) {
        int realNextInt = ((Random)random).nextInt(bound);
        if (ContextManager.needReplay()) {
            Mocker mocker = createNextIntMocker(bound);
            Mocker replayMocker = MockUtils.replayMocker(mocker);
            if (replayResultIsNull(replayMocker)) {
                return realNextInt;
            }
            return Integer.parseInt(replayMocker.getTargetResponse().getBody());
        }
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
    // endregion
}