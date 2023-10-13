package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;

public class CaseManager {
    private CaseManager() {
    }

    public static void invalid(String recordId, String operationName) {
        try {
            final ArexContext context = ContextManager.getRecordContext(recordId);
            if (context == null || context.isInvalidCase()) {
                return;
            }
            LogManager.warn("invalidCase",
                    StringUtil.format("invalid case: recordId: %s operation: %s", recordId,  operationName));
            context.setInvalidCase(true);
        } catch (Exception ex) {
            LogManager.warn("invalidCase.remove", ex);
        }
    }

    public static boolean isInvalidCase(String recordId) {
        final ArexContext context = ContextManager.getRecordContext(recordId);
        if (context == null) {
            return false;
        }
        return context.isInvalidCase();
    }
}
