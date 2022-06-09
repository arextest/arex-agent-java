package io.arex.inst.time;

import io.arex.agent.bootstrap.cache.TimeCache;
import net.bytebuddy.asm.Advice;

public class TimeMachineInterceptor {

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static long onEnter() {
        return TimeCache.get();
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Enter long replay, @Advice.Return(readOnly = false) long result) {
        if (replay > 0) {
            result = replay;
        }
    }
}
