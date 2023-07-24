package io.arex.foundation.util.httpclient.async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactoryImpl
 *
 *
 * @date 2021/02/04
 */
public class ThreadFactoryImpl implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final ThreadGroup group;
    protected final boolean daemon;

    public ThreadFactoryImpl(final String namePrefix) {
        this(namePrefix, false);
    }

    public ThreadFactoryImpl(final String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;

        SecurityManager s = System.getSecurityManager();
        this.group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        String name = String.format("arex-%s-%d-thread-%d", this.namePrefix, POOL_NUMBER.getAndIncrement(), threadNumber.getAndIncrement());
        Thread thread = new Thread(this.group, runnable, name);
        thread.setDaemon(this.daemon);
        return thread;
    }
}

