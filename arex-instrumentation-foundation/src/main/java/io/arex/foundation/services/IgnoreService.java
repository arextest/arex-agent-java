package io.arex.foundation.services;

import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.arex.agent.bootstrap.util.StringUtil;

import java.util.*;


public class IgnoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreService.class);
    private static final Set<String> ignoreServices = new HashSet<>();
    private static final Set<String> enablePackages = new HashSet<>();

    static {
        ignoreServices.add("POST /artemis-discovery-service/api/discovery/lookup.json");
        ignoreServices.add("POST /api/CMSGetServer");
        ignoreServices.add("POST /api/storage/record/query");
        ignoreServices.add("POST /api/storage/record/save");
    }

    /**
     * Register a service that will not capture data and playback
     * @param method: http method, like POST
     * @param url: service address(No need for ip and port), like /api/XxxService
     */
    public static void registerIgnoreService(String method, String url) {
        if (StringUtil.isEmpty(method) || StringUtil.isEmpty(url)) {
            return;
        }

        ignoreServices.add(method + " " + url);
    }

    /**
     * Enable the target mock by config
     * default return true
     */
    public static boolean isServiceEnabled(String targetName) {
        return !ignoreServices.contains(targetName);
    }

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
}
