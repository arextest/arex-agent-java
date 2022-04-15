package io.arex.foundation.internal;


import io.arex.foundation.model.AbstractMocker;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date 2021/8/16
 */
public class MockerRingBuffer {
    private final static int DEFAULT_SIZE = 1024;
    private AbstractMocker[] buffer;
    private int head = 0;
    private volatile int tail = 0;
    private final int bufferSize;
    private SpinLock locker = new SpinLock();

    public MockerRingBuffer(){
        this(DEFAULT_SIZE);
    }

    public MockerRingBuffer(int initSize){
        this.bufferSize = initSize;
        this.buffer = new AbstractMocker[bufferSize];
    }

    private boolean empty() {
        return head == tail;
    }

    private boolean full() {
        return (tail + 1) % bufferSize == head;
    }

    public void clear(){
        Arrays.fill(buffer,null);
        this.head = 0;
        this.tail = 0;
    }

    public boolean put(AbstractMocker v) {
        try {
            locker.lock();
            if (full()) {
                return false;
            }

            buffer[tail] = v;
            tail = (tail + 1) % bufferSize;
            return true;
        } finally {
            locker.unLock();
        }
    }

    public void putOnException(AbstractMocker v) {
        ExceptionCaseBuffer.INSTANCE.put(v.getCaseId());
    }

    /**
     * Single-threaded
     */
    public AbstractMocker get() {
        if (empty()) {
            return null;
        }

        AbstractMocker result = buffer[head];
        buffer[head] = null;
        head = (head + 1) % bufferSize;
        if (ExceptionCaseBuffer.INSTANCE.contains(result.getCaseId())) {
            result = get();
        }
        return result;
    }

    class SpinLock {
        final AtomicInteger locker = new AtomicInteger(0);
        long heldThread = 0;

        final void lock() {
            long currentThread = Thread.currentThread().getId();
            if (currentThread == heldThread) {
                return;
            }

            while (!locker.compareAndSet(0, 1)) {}
            heldThread = currentThread;
        }

        final void unLock() {
            heldThread = 0;
            locker.set(0);
        }
    }

    /**
     * Low conflict cost
     */
    static class ExceptionCaseBuffer {
        private static final ExceptionCaseBuffer INSTANCE = new ExceptionCaseBuffer();
        private final ConcurrentHashMap<String, Long> exceptionIDs = new ConcurrentHashMap<String, Long>(10);
        private final String[] caseIds = new String[10];
        private int index = 0;
        private long lastNanos = 0;

        boolean contains(String traceId) {
            if (index > 0) {
                purge();
                for (int i = 0; i < 10; i++) {
                    if (caseIds[i].equals(traceId)) {
                        return true;
                    }
                }
            }
            return false;
        }

        void put(String caseId) {
            purge();
            caseIds[index] = caseId;
            index = index++ % 10;
            lastNanos = System.nanoTime();
        }

        void purge() {
            if (System.nanoTime() - lastNanos > 300000000) {
                for (int i = 0; i < index; i++) {
                    caseIds[i] = null;
                }
                index = 0;
            }
        }

    }

    public int length(){
        return tail - head;
    }
}
