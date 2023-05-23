package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.TraceContextManager;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;


public class CallableWrapper<V> implements Callable<V> {

    private final TraceTransmitter traceTransmitter;
    private final Callable<V> callable;

    private CallableWrapper(Callable<V> callable) {
        this.traceTransmitter = TraceTransmitter.create();
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            return callable.call();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallableWrapper<?> that = (CallableWrapper<?>) o;

        return callable.equals(that.callable);
    }

    @Override
    public int hashCode() {
        return callable.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + callable.toString();
    }

    public static <T> Callable<T> get(Callable<T> callable) {
        if (null == callable || TraceContextManager.get() == null) {
            return callable;
        }

        if (callable instanceof CallableWrapper || callable instanceof ForkJoinTask) {
            return callable;
        }
        return new CallableWrapper<T>(callable);
    }
}
