package io.arex.foundation.internal;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class MockEntityBuffer {
    private static final int DEFAULT_SIZE = 1024;
    private DataEntity[] buffer;
    private int head = 0;
    private volatile int tail = 0;
    private int bufferSize;
    private SpinLock locker = new SpinLock();

    public MockEntityBuffer(){
        this(DEFAULT_SIZE);
    }

    public MockEntityBuffer(int initSize){
        this.bufferSize = initSize;
        this.buffer = new DataEntity[bufferSize];
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

    public boolean put(DataEntity v) {
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

    /**
     * Single-threaded
     */
    public DataEntity get() {
        if (empty()) {
            return null;
        }

        DataEntity result = buffer[head];
        buffer[head] = null;
        head = (head + 1) % bufferSize;
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

    public int length(){
        return tail - head;
    }
}
