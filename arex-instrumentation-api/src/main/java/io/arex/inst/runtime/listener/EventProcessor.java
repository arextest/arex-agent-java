package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.Constants;
import io.arex.inst.runtime.model.DynamicClassMocker;
import io.arex.inst.runtime.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);

    public static void onCreate(){
        initClock();
    }

    private static void initClock(){
        try {
            if (ContextManager.needReplay() && Config.get().getBoolean("arex.time.machine", false)) {
                DynamicClassMocker mocker = new DynamicClassMocker(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD, null);
                Object result = mocker.replay();
                long millis = Long.parseLong(String.valueOf(result));
                if (millis > 0) {
                    TimeCache.put(millis);
                }
            } else if (ContextManager.needRecord()) {
                DynamicClassMocker mocker = new DynamicClassMocker(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD,
                        null, String.valueOf(System.currentTimeMillis()), Long.class.getName());
                mocker.record();
            }
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("time.machine.init"), e);
        }
    }

    public static void onExit(){
        //TimeCache.remove();
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     */
    public static void onRequest(){
        TimeCache.remove();
        TraceContextManager.remove();
    }
}
