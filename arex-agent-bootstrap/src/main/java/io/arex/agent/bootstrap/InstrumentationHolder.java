package io.arex.agent.bootstrap;

import java.lang.instrument.Instrumentation;

public class InstrumentationHolder {
    private static volatile Instrumentation instrumentation;

    private static volatile ClassLoader agentClassLoader;

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        InstrumentationHolder.instrumentation = instrumentation;
    }

    public static ClassLoader getAgentClassLoader() {
        return agentClassLoader;
    }

    public static void setAgentClassLoader(ClassLoader loader) {
        agentClassLoader = loader;
    }
}
