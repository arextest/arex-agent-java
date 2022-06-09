package io.arex.agent.bootstrap.ctx;

import java.util.concurrent.atomic.AtomicReference;

public class TraceTransmitter implements AutoCloseable {

    private final AtomicReference<Object> captureRef;
    private AtomicReference<Object> backupRef;

    private TraceTransmitter() {
        this.captureRef = new AtomicReference<>(ArexThreadLocal.Transmitter.capture());
        this.backupRef = new AtomicReference<>();
    }

    public TraceTransmitter transmit() {
        Object capture = captureRef.getAndSet(null);
        if (capture != null) {
            backupRef.set(ArexThreadLocal.Transmitter.replay(capture));
        }
        return this;
    }

    @Override
    public void close() {
        Object backup = backupRef.getAndSet(null);
        if (backup != null) {
            ArexThreadLocal.Transmitter.restore(backup);
        }
    }

    public static TraceTransmitter create() {
        return new TraceTransmitter();
    }
 }