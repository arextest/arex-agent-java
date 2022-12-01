package io.arex.foundation.listener;

import com.arextest.model.mock.Mocker;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseInitializer.class);

    public static void initialize(EventSource source){
        initContext(source);
        initClock();
    }

    public static void initContext(EventSource source){
        ContextManager.overdueCleanUp();
        ArexContext context = ContextManager.currentContext(true, source.getCaseId());
        if (context != null) {
            context.setExcludeMockTemplate(SerializeUtils.deserialize(source.getExcludeMockTemplate(), Constants.EXCLUDE_MOCK_TYPE));
        }
    }

    public static void initClock() {
        try {
            if (ConfigManager.INSTANCE.startTimeMachine() && ContextManager.needReplay()) {
                Mocker mocker = MockerUtils.createDynamicClass(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD);
                Object result = MockerUtils.replayBody(mocker);
                long millis = NumberUtils.toLong(String.valueOf(result), 0);
                if (millis > 0) {
                    TimeCache.put(millis);
                }
            } else if (ContextManager.needRecord()) {
                Mocker mocker = MockerUtils.createDynamicClass(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD);
                mocker.getTargetResponse().setBody(String.valueOf(System.currentTimeMillis()));
                mocker.getTargetResponse().setType(Long.class.getName());
                MockerUtils.record(mocker);

            }
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("time.machine.init"), e);
        }
    }

    public static void release() {
        //TimeCache.remove();
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     */
    public static void onEnter() {
        TimeCache.remove();
        TraceContextManager.remove();
    }

    public static boolean exceedRecordRate(String path) {
        if (ConfigManager.INSTANCE.invalid()) {
            return true;
        }

        return !ConfigManager.INSTANCE.isEnableDebug()
                && !HealthManager.acquire(path, ConfigManager.INSTANCE.getRecordRate());
    }
}