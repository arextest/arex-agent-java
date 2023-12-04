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

    public static void invalid(
            String recordId, String replayId, String operationName, String invalidReason) {
        try {
            final ArexContext context =
                    StringUtil.isNotEmpty(replayId)
                            ? ContextManager.getContext(replayId)
                            : ContextManager.getContext(recordId);
            if (context == null || context.isInvalidCase()) {
                return;
            }
            context.setInvalidCase(true);
            String invalidCaseJson =
                    StringUtil.format(
                            "{\"appId\":\"%s\",\"recordId\":\"%s\",\"replayId\":\"%s\",\"reason\":\"%s\"}",
                            System.getProperty(ConfigConstants.SERVICE_NAME), recordId, replayId, invalidReason);
            DataService.INSTANCE.invalidCase(invalidCaseJson);
            LogManager.warn("invalidCase",
                    StringUtil.format("invalid case: recordId: %s, replayId: %s, operation: %s, reason: %s", recordId, replayId, operationName, invalidReason));
        } catch (Exception ex) {
            LogManager.warn("invalidCase.remove", ex);
        }
    }

    public static boolean isInvalidCase(String traceId) {
        if (StringUtil.isEmpty(traceId)) {
            return true;
        }
        final ArexContext context = ContextManager.getContext(traceId);
        if (context == null) {
            return false;
        }
        return context.isInvalidCase();
    }
}
