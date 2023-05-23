package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.TraceContextManager;

import java.util.concurrent.ForkJoinTask;


public class RunnableWrapper implements Runnable {
    private final Runnable runnable;
    private final TraceTransmitter traceTransmitter;

    private RunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
        this.traceTransmitter = TraceTransmitter.create();
    }

    @Override
    public void run() {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            runnable.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RunnableWrapper that = (RunnableWrapper) o;
        return runnable.equals(that.runnable);
    }

    @Override
    public int hashCode() {
        return runnable.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + runnable.toString();
    }

    public static Runnable get(Runnable runnable) {
        if (null == runnable  || TraceContextManager.get() == null) {
            return runnable;
        }

        if (runnable instanceof RunnableWrapper || runnable instanceof ForkJoinTask) {
            return runnable;
        }
        return new RunnableWrapper(runnable);
    }
}
