package io.arex.inst.runtime.util;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.arex.agent.bootstrap.util.StringUtil;

import java.util.*;


public class IgnoreUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreUtils.class);
    private static final String SEPARATOR_STAR = "*";

    public static boolean ignoreMockResult(String serviceKey, String operationKey) {
        if (StringUtil.isEmpty(serviceKey)) {
            return false;
        }
        ArexContext context = ContextManager.currentContext();
        if (context == null || context.getExcludeMockTemplate() == null) {
            return false;
        }
        Map<String, Set<String>> excludeMockTemplate = context.getExcludeMockTemplate();
        if (!excludeMockTemplate.containsKey(serviceKey)) {
            return false;
        }
        Set<String> operationSet = excludeMockTemplate.get(serviceKey);
        // If empty, this service all operations ignore mock result
        if (operationSet == null || operationSet.isEmpty()) {
            LOGGER.info("{}service:{} all operations ignore mock result", LogUtil.buildTitle("ignoreMock"), serviceKey);
            return true;
        }
        // Specified operation ignore mock result
        if (operationSet.contains(operationKey)) {
            LOGGER.info("{}operation:{}.{} ignore mock result", LogUtil.buildTitle("ignoreMock"), serviceKey, operationKey);
            return true;
        }
        return false;
    }

    /**
     * Register a service that will not capture data and playback
     */
    public static boolean ignoreOperation(String targetName) {
        if (StringUtil.isEmpty(targetName) || Config.get() == null) {
            return false;
        }

        Set<String> excludePathList = Config.get().excludeServiceOperations();
        if (excludePathList == null || excludePathList.isEmpty()) {
            return false;
        }

        for (String excludePath : excludePathList) {
            if (excludePath.equalsIgnoreCase(targetName)) {
                return true;
            }

            if (excludePath.startsWith(SEPARATOR_STAR) &&
                targetName.endsWith(excludePath.substring(1))) {
                return true;
            }
            if (excludePath.endsWith(SEPARATOR_STAR) &&
                targetName.startsWith(excludePath.substring(0, excludePath.length() - 1))) {
                return true;
            }
        }

        return false;
    }
}
