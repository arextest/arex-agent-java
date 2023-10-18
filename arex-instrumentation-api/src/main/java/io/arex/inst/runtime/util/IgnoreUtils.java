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
     * Include the operation that need to record or replay
     */
    public static boolean includeOperation(String targetName) {
        if (StringUtil.isEmpty(targetName) || Config.get() == null) {
            return false;
        }

        Set<String> includeServiceOperations = Config.get().getIncludeServiceOperations();
        return operationMatched(targetName, includeServiceOperations);
    }

    /**
     * Exclude the operation that not need to record or replay
     */
    public static boolean excludeOperation(String targetName) {
        if (StringUtil.isEmpty(targetName) || Config.get() == null) {
            return false;
        }
        Set<String> excludeServiceOperations = Config.get().excludeServiceOperations();
        boolean isOperationMatched = operationMatched(targetName, excludeServiceOperations);
        if (isOperationMatched && ContextManager.needReplay()) {
            LogManager.warn("replay.hitBlockList", StringUtil.format("Hit block list, target name: %s", targetName));
        }
        return isOperationMatched;
    }

    /**
     * Exclude entrance operation by includeServiceOperations and excludeServiceOperations.
     * First if includeServiceOperations is not empty, only use excludeServiceOperations to judge.
     * Second if includeServiceOperations is empty, use excludeServiceOperations to jude.
     */
    public static boolean excludeEntranceOperation(String targetName) {
        if (Config.get() != null && CollectionUtil.isNotEmpty(Config.get().getIncludeServiceOperations())) {
            return !includeOperation(targetName);
        }
        return excludeOperation(targetName);
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
            // startWith * and endWith *
            if (searchOperation.length() > 2 &&
                searchOperation.startsWith(SEPARATOR_STAR) &&
                searchOperation.endsWith(SEPARATOR_STAR) &&
                targetName.contains(searchOperation.substring(1, searchOperation.length() - 1))) {
                return true;
            }
            if (searchOperation.length() > 1) {
                if (searchOperation.startsWith(SEPARATOR_STAR) &&
                    targetName.endsWith(searchOperation.substring(1))) {
                    return true;
                }
                if (searchOperation.endsWith(SEPARATOR_STAR) &&
                    targetName.startsWith(searchOperation.substring(0, searchOperation.length() - 1))) {
                    return true;
                }
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
