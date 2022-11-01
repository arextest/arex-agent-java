package io.arex.foundation.util;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;

public class PreProcessor {
    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     */
    public static void onEnter(){
        TraceContextManager.remove();
    }

    public static boolean exceedRecordRate(String recordId, String path) {
        return StringUtil.isEmpty(recordId)
                && !ConfigManager.INSTANCE.isEnableDebug()
                && !HealthManager.acquire(path, ConfigManager.INSTANCE.getRecordRate());
    }
}
