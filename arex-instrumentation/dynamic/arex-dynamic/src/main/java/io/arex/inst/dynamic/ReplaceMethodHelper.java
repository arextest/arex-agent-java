package io.arex.inst.dynamic;

import com.arextest.model.mock.Mocker;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.MockerUtils;
import org.apache.commons.lang3.StringUtils;

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
            Mocker mocker = makeMocker();
            return UUID.fromString(String.valueOf(MockerUtils.replayBody(mocker)));
        }
        UUID realUuid = UUID.randomUUID();
        if (ContextManager.needRecord()) {
            Mocker mocker = makeMocker();
            mocker.getTargetResponse().setType(String.class.getName());
            mocker.getTargetResponse().setBody(realUuid.toString());
            MockerUtils.record(mocker);
        }
        return realUuid;
    }

    private static Mocker makeMocker() {
        Mocker mocker = MockerUtils.createDynamicClass(UUID.class.getName(), "randomUUID");
        mocker.getTargetRequest().setBody(StringUtils.EMPTY);
        return mocker;
    }
}