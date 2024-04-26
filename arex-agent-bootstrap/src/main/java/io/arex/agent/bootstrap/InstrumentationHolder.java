package io.arex.agent.bootstrap;

import java.io.File;
import java.lang.instrument.Instrumentation;

public class InstrumentationHolder {
    private static Instrumentation instrumentation;

    private static ClassLoader agentClassLoader;

    private static File agentFile;

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

    public static File getAgentFile() {
        return agentFile;
    }

    public static void setAgentFile(File agentFile) {
        InstrumentationHolder.agentFile = agentFile;
    }
}
