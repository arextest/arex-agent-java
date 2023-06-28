package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.ConcurrentHashSet;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.agent.bootstrap.util.StringUtil;

import java.util.*;


public class IgnoreUtils {
    private static final String SEPARATOR_STAR = "*";
    /**
     *  operation cache: can not serialize args or response
     */
    private static final Set<Integer> INVALID_OPERATION_HASH_CACHE = new ConcurrentHashSet<>();

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
            LogManager.info("ignoreMock", StringUtil.format("service:%s all operations ignore mock result", serviceKey));
            return true;
        }
        // Specified operation ignore mock result
        if (operationSet.contains(operationKey)) {
            LogManager.info("ignoreMock", StringUtil.format("operation:%s.%s ignore mock result", serviceKey, operationKey));
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

        Set<String> includeServiceOperations = Config.get().getIncludeServiceOperations();
        if (CollectionUtil.isNotEmpty(includeServiceOperations)) {
            return !operationMatched(targetName, includeServiceOperations);
        }
        Set<String> excludePathList = Config.get().excludeServiceOperations();
        return operationMatched(targetName, excludePathList);
    }

    /**
     * targetName match searchOperations
     * @param searchOperations: includeServiceOperations or excludeServiceOperations.
     * @return includeServiceOperations: true -> notNeedIgnore, excludeServiceOperations: true -> needIgnore
      */
    private static boolean operationMatched(String targetName, Set<String> searchOperations) {
        if (CollectionUtil.isEmpty(searchOperations)) {
            return false;
        }
        for (String searchOperation : searchOperations) {
            if (searchOperation.equalsIgnoreCase(targetName)) {
                return true;
            }
            if (searchOperation.startsWith(SEPARATOR_STAR) &&
                targetName.endsWith(searchOperation.substring(1))) {
                return true;
            }
            if (searchOperation.endsWith(SEPARATOR_STAR) &&
                targetName.startsWith(searchOperation.substring(0, searchOperation.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    public static boolean invalidOperation(String operationSignature) {
        return INVALID_OPERATION_HASH_CACHE.contains(operationSignature.hashCode());
    }

    public static void addInvalidOperation(String operationSignature) {
        INVALID_OPERATION_HASH_CACHE.add(operationSignature.hashCode());
    }

}
