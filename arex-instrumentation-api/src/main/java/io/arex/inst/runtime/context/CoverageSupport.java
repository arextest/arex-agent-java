package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.internal.converage.ExecutionPathBuilder;

public class CoverageSupport {

    public static void enter(int methodKey) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodEnter(methodKey);
        }
    }

    public static void execute(int methodKey, int code) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodExecute(methodKey, code);
        }
    }

    public static void exit(int methodKey) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodExit(methodKey);
        }
    }

    public static void exit(final String debugMessage, int methodKey) {
        ExecutionPathBuilder builder = getExecutionPathBuilder();
        if (builder != null) {
            builder.methodExit(methodKey, debugMessage);
        }
    }

    private static ExecutionPathBuilder getExecutionPathBuilder() {
        ArexContext context = ContextManager.currentContext();
        return context != null ? context.getExecutionPathBuilder() : null;
    }
}
