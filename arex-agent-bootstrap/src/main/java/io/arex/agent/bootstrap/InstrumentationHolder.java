package io.arex.agent.bootstrap;

import java.lang.instrument.Instrumentation;

public class InstrumentationHolder {
    private static volatile Instrumentation instrumentation;

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        InstrumentationHolder.instrumentation = instrumentation;
    }
}
