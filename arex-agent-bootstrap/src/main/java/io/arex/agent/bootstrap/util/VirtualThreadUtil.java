package io.arex.agent.bootstrap.util;

/**
 * Loom(virtual thread) related helper.
 *
 * <p>Compiled with java 8, so neither {@code Thread.isVirtual()} nor
 * {@code jdk.internal.misc.CarrierThread} can be referenced directly.
 */
public final class VirtualThreadUtil {

    /**
     * The thread type of the default virtual thread scheduler's worker,
     * see {@code java.lang.VirtualThread#createDefaultScheduler()}.
     */
    private static final String CARRIER_THREAD = "jdk.internal.misc.CarrierThread";

    private VirtualThreadUtil() {
    }

    /**
     * Identifies a default-scheduler worker while it is running as a platform
     * thread. While a virtual thread is mounted, {@link Thread#currentThread()}
     * returns the virtual thread, so this check does not skip its business tasks.
     *
     * <p>ForkJoinTask advice runs outside the virtual thread's continuation and
     * must not transmit context to the carrier's separate thread locals. It must
     * also avoid polling the shared captured-context cache's
     * {@link java.lang.ref.ReferenceQueue}: on JDK 21, a virtual thread can be the
     * first waiter for that queue's lock. If all carriers then block on the same
     * lock, the virtual thread cannot resume even after the lock is released.
     *
     * <p>This identifies the JDK's default scheduler only, not custom schedulers.
     *
     * @return true if the current thread is a carrier thread of the default scheduler
     */
    public static boolean isCarrierThread() {
        return isCarrierThread(Thread.currentThread());
    }

    public static boolean isCarrierThread(Thread thread) {
        return thread != null && isCarrierThread(thread.getClass().getName());
    }

    static boolean isCarrierThread(String threadClassName) {
        return CARRIER_THREAD.equals(threadClassName);
    }
}
