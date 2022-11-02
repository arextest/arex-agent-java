package io.arex.foundation.listener;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.DynamicClassMocker;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseInitializer.class);

    public static void initialize(String caseId){
        initContext(caseId);
        initClock();
    }

    public static void initContext(String caseId){
        ContextManager.overdueCleanUp();
        ContextManager.currentContext(true, caseId);
    }

    public static void initClock(){
        try {
            if (ConfigManager.INSTANCE.startTimeMachine() && ContextManager.needReplay()) {
                DynamicClassMocker mocker = new DynamicClassMocker(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD, null);
                Object result = mocker.replay();
                long millis = NumberUtils.toLong(String.valueOf(result), 0);
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

    public static void release(){
        //TimeCache.remove();
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     */
    public static void onEnter(){
        TimeCache.remove();
        TraceContextManager.remove();
    }

    public static boolean exceedRecordRate(String recordId, String path) {
        if (ConfigManager.INSTANCE.invalid()) {
            return true;
        }

        return StringUtil.isEmpty(recordId)
                && !ConfigManager.INSTANCE.isEnableDebug()
                && !HealthManager.acquire(path, ConfigManager.INSTANCE.getRecordRate());
    }
}
