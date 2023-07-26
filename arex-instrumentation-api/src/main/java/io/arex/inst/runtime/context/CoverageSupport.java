package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.internal.converage.ExecutionPathBuilder;

public class CoverageSupport {

    public static void enter(final int methodKey) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodEnter(methodKey);
        }
    }

    public static void execute(final int methodKey, int line) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodExecute(methodKey, line);
        }
    }
    public static void exit(final int methodKey) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodExit(methodKey);
        }
    }

    private static ExecutionPathBuilder getExecutionPathBuilder() {
        ArexContext context = ContextManager.currentContext();
        return context != null ? context.getExecutionPathBuilder() : null;
    }
}
