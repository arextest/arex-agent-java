package io.arex.inst.executors;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.internal.Cache;
import io.arex.agent.bootstrap.util.VirtualThreadUtil;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Uses separate JVMs because the reproducer deliberately starves every carrier.
 * Reflection keeps the test sources compatible with the project's Java 8 target.
 */
class ForkJoinCarrierThreadTest {
    @TempDir
    Path directory;

    @Test
    void reproducesStarvationWithoutCarrierGuard() throws Exception {
        runScenario("before");
    }

    @Test
    void carrierGuardsAllowVirtualThreadToResume() throws Exception {
        runScenario("after");
    }

    private void runScenario(String mode) throws Exception {
        assumeTrue("21".equals(System.getProperty("java.specification.version")),
                "This reproducer targets JDK 21's ReentrantLock-based ReferenceQueue");
        assumeTrue(ReferenceQueue.class.getDeclaredField("lock").getType() == ReentrantLock.class);
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Premain-Class", TestAgent.class.getName());
        attributes.putValue("Can-Retransform-Classes", "true");
        Path agent = directory.resolve("carrier-test-agent.jar");
        // The agent class and its dependencies are supplied by the test classpath.
        try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(agent), manifest)) {
        }
        String bootstrap = new File(Cache.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Path log = directory.resolve(mode + ".log");
        Process process = new ProcessBuilder(
                new File(System.getProperty("java.home"), "bin/java").getPath(),
                "-Djdk.virtualThreadScheduler.parallelism=2",
                "-Djdk.virtualThreadScheduler.maxPoolSize=2",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.ref=ALL-UNNAMED",
                "-Xbootclasspath/a:" + bootstrap,
                "-javaagent:" + agent + "=" + mode,
                "-cp", classpath, Scenario.class.getName(), mode)
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            System.out.println(output);
            assertTrue(finished, "Child JVM timed out:\n" + output);
            assertEquals(0, process.exitValue(), output);
            assertTrue(output.contains("RESULT " + mode + " PASS"), output);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    public static class TestAgent {
        public static void premain(String mode, Instrumentation instrumentation) {
            Class<?> execution = "before".equals(mode) ? BeforeExec.class
                    : ForkJoinTaskInstrumentation.ExecAdvice.class;
            Class<?> constructor = "before".equals(mode) ? BeforeConstructor.class
                    : ForkJoinTaskConstructorInstrumentation.ConstructorAdvice.class;
            new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("io.arex.")))
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
                    .type(new ForkJoinTaskInstrumentation().typeMatcher())
                    .transform((builder, type, loader, module, domain) -> {
                        if (new ForkJoinTaskConstructorInstrumentation().typeMatcher().matches(type)) {
                            builder = builder.visit(Advice.to(constructor).on(isConstructor()));
                        }
                        return builder.visit(Advice.to(execution).on(
                                new ForkJoinTaskInstrumentation().methodAdvices().get(0).getMethodMatcher()));
                    })
                    .assureReadEdgeFromAndTo(instrumentation, Cache.class)
                    .installOn(instrumentation);
        }
    }

    // The unguarded advice is a positive control: the same schedule must reproduce the old bug.
    public static class BeforeExec {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.This Object task, @Advice.Local("backup") Object backup) {
            backup = ArexThreadLocal.Transmitter.replay(Cache.CAPTURED_CACHE.get(task));
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Local("backup") Object backup) {
            ArexThreadLocal.Transmitter.restore(backup);
        }
    }

    public static class BeforeConstructor {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This Object task) {
            if (TraceContextManager.get() == null) {
                return;
            }
            Object captured = ArexThreadLocal.Transmitter.capture();
            if (captured != null) {
                Cache.CAPTURED_CACHE.put(task, captured);
            }
        }
    }

    public static class Scenario {
        private static volatile boolean enterQueue;

        public static void main(String[] args) throws Exception {
            String mode = args[0];
            startVirtualThread(() -> {}).join();
            // Load the adapted-task class before arranging lock contention.
            ForkJoinTask.adapt(() -> {});
            Object key = new Object();
            Cache.CAPTURED_CACHE.put(key, "cached-value");
            @SuppressWarnings("unchecked")
            ReferenceQueue<Object> queue = (ReferenceQueue<Object>) Cache.CAPTURED_CACHE;
            Field lockField = ReferenceQueue.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            ReentrantLock lock = (ReentrantLock) lockField.get(queue);
            Field schedulerField = Class.forName("java.lang.VirtualThread").getDeclaredField("DEFAULT_SCHEDULER");
            schedulerField.setAccessible(true);
            ForkJoinPool scheduler = (ForkJoinPool) schedulerField.get(null);
            CountDownLatch running = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            CountDownLatch probes = new CountDownLatch(2);
            Thread vt = startVirtualThread(() -> {
                check(!VirtualThreadUtil.isCarrierThread(), "VT must not be mistaken for a carrier");
                TraceContextManager.set("vt-context");
                running.countDown();
                while (!enterQueue) {
                    // Stay mounted until the main thread holds the ReferenceQueue lock.
                }
                check("cached-value".equals(Cache.CAPTURED_CACHE.get(key)), "cached value was lost");
                check("vt-context".equals(TraceContextManager.get()), "VT context was lost");
                done.countDown();
            });
            check(running.await(5, TimeUnit.SECONDS), "VT did not start");
            lock.lock();
            try {
                // An empty ReferenceQueue.poll() never takes the lock. Explicit enqueue avoids GC races.
                new WeakReference<>(new Object(), queue).enqueue();
                enterQueue = true;
                await(() -> lock.hasQueuedThread(vt) && vt.getState() == Thread.State.WAITING,
                        "VT must be the first parked lock waiter");
                Runnable probe = () -> {
                    check(VirtualThreadUtil.isCarrierThread(), "probe must run as a real carrier");
                    // Exercise the constructor guard with non-null carrier context as well.
                    TraceContextManager.set("carrier-context");
                    try {
                        ForkJoinTask.adapt(() -> {});
                    } finally {
                        TraceContextManager.remove();
                    }
                    probes.countDown();
                };
                scheduler.execute(probe);
                scheduler.execute(probe);
                if ("before".equals(mode)) {
                    await(() -> lock.getQueueLength() == 3, "VT followed by two blocked carriers");
                } else {
                    check(probes.await(5, TimeUnit.SECONDS), "carrier advice blocked on the cache");
                    check(lock.getQueueLength() == 1, "only the VT should wait for the lock");
                }
                System.out.println("mode=" + mode + " before unlock: queueLength=" + lock.getQueueLength()
                        + " vtQueued=" + lock.hasQueuedThread(vt) + " poolSize=" + scheduler.getPoolSize()
                        + " probesRemaining=" + probes.getCount());
            } finally {
                lock.unlock();
            }
            boolean progressed = done.await(2, TimeUnit.SECONDS);
            System.out.println("after unlock: progressed=" + progressed + " lockHeld=" + lock.isLocked()
                    + " vtState=" + vt.getState() + " queueLength=" + lock.getQueueLength()
                    + " probesRemaining=" + probes.getCount());
            if ("before".equals(mode)) {
                check(!progressed && !lock.isLocked() && lock.getQueueLength() == 3
                        && vt.getState() == Thread.State.RUNNABLE && probes.getCount() == 2,
                        "expected an unlocked queue with a runnable VT and both carriers blocked");
                Thread.getAllStackTraces().forEach((thread, stack) -> {
                    if (VirtualThreadUtil.isCarrierThread(thread)) {
                        System.out.println(thread);
                        for (StackTraceElement frame : stack) {
                            System.out.println("    " + frame);
                        }
                    }
                });
            } else {
                check(progressed, "VT did not resume after unlock");
                check(lock.getQueueLength() == 0, "queue did not drain");
                verifyOrdinaryForkJoinContext();
            }
            System.out.println("RESULT " + mode + " PASS");
            // The before case deliberately leaves the scheduler's daemon carriers blocked.
        }

        private static Thread startVirtualThread(Runnable task) throws Exception {
            return (Thread) Thread.class.getMethod("startVirtualThread", Runnable.class).invoke(null, task);
        }

        private static void verifyOrdinaryForkJoinContext() throws Exception {
            ForkJoinPool pool = new ForkJoinPool(1);
            TraceContextManager.set("parent-context");
            try {
                String captured = pool.submit(() -> {
                    check(!VirtualThreadUtil.isCarrierThread(), "ordinary FJP worker mistaken for carrier");
                    return TraceContextManager.get();
                }).get(5, TimeUnit.SECONDS);
                check("parent-context".equals(captured), "ordinary FJP context propagation regressed");
            } finally {
                TraceContextManager.remove();
                pool.shutdownNow();
            }
        }

        private static void await(BooleanSupplier condition, String message) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!condition.getAsBoolean()) {
                check(System.nanoTime() < deadline, message);
                Thread.sleep(1);
            }
        }

        private static void check(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }
    }
}
