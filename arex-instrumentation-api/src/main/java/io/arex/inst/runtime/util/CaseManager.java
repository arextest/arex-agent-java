package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.service.DataService;

public class CaseManager {

    private CaseManager() {
    }

    public static void invalid(String recordId, String operationName, String invalidReason) {
        try {
            final ArexContext context = ContextManager.getRecordContext(recordId);
            if (context == null || context.isInvalidCase()) {
                return;
            }
            context.setInvalidCase(true);
            String invalidCaseJson = StringUtil.format("{\"appId\":\"%s\",\"recordId\":\"%s\",\"reason\":\"%s\"}",
                    System.getProperty(ConfigConstants.SERVICE_NAME), recordId, invalidReason);
            DataService.INSTANCE.invalidCase(invalidCaseJson);
            LogManager.warn("invalidCase",
                    StringUtil.format("invalid case: recordId: %s operation: %s reason: %s", recordId,  operationName, invalidReason));
        } catch (Exception ex) {
            LogManager.warn("invalidCase.remove", ex);
        }
    }

    public static boolean isInvalidCase(String recordId) {
        if (StringUtil.isEmpty(recordId)) {
            return true;
        }
        final ArexContext context = ContextManager.getRecordContext(recordId);
        if (context == null) {
            return false;
        }
        return context.isInvalidCase();
    }
}
