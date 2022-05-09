package io.arex.inst.executors;

import io.arex.api.instrumentation.MethodInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import io.arex.agent.bootstrap.ctx.CallableWrapper;
import io.arex.agent.bootstrap.ctx.RunnableWrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ThreadPoolInstrumentation extends TypeInstrumentation {
    private List<String> includeExecutors = null;

    public ThreadPoolInstrumentation() {
        this.includeExecutors = includeExecutors();
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return (t) -> includeExecutors.contains(t.getName());
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(buildCallableAdvice(),
                buildRunnableAdvice());
    }

    private MethodInstrumentation buildRunnableAdvice() {
        return new MethodInstrumentation(
                isMethod().and(isPublic()).and(not(isStatic()))
                        .and(takesArgument(0, Runnable.class)),
                this.getClass().getName() + "$ExecutorRunnableAdvice");
    }

    private MethodInstrumentation buildCallableAdvice() {
        return new MethodInstrumentation(
                isMethod().and(isPublic()).and(not(isStatic()))
                        .and(takesArgument(0, Callable.class)),
                this.getClass().getName() + "$ExecutorCallableAdvice");
    }

    private List<String> includeExecutors() {
        return asList(
                "java.util.concurrent.ThreadPoolExecutor",
                "java.util.concurrent.AbstractExecutorService",
                "java.util.concurrent.CompletableFuture$ThreadPerTaskExecutor",
                "java.util.concurrent.Executors$DelegatedExecutorService",
                "java.util.concurrent.Executors$FinalizableDelegatedExecutorService",
                "org.apache.tomcat.util.threads.ThreadPoolExecutor",
                "org.eclipse.jetty.util.thread.QueuedThreadPool",
                "org.eclipse.jetty.util.thread.ReservedThreadExecutor",
                "com.google.common.util.concurrent.AbstractListeningExecutorService",
                "com.google.common.util.concurrent.MoreExecutors$ListeningDecorator",
                "io.netty.channel.epoll.EpollEventLoop",
                "io.netty.channel.epoll.EpollEventLoopGroup",
                "io.netty.channel.MultithreadEventLoopGroup",
                "io.netty.channel.nio.NioEventLoop",
                "io.netty.channel.nio.NioEventLoopGroup",
                "io.netty.util.concurrent.AbstractEventExecutor",
                "io.netty.util.concurrent.AbstractEventExecutorGroup",
                "io.netty.util.concurrent.AbstractScheduledEventExecutor",
                "io.netty.util.concurrent.DefaultEventExecutor",
                "io.netty.util.concurrent.DefaultEventExecutorGroup",
                "io.netty.util.concurrent.GlobalEventExecutor",
                "io.netty.util.concurrent.MultithreadEventExecutorGroup",
                "io.netty.util.concurrent.SingleThreadEventExecutor"
        );
    }

    @SuppressWarnings("unused")
    public static class ExecutorRunnableAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(
                @Advice.Argument(value = 0, readOnly = false, optional = true) Runnable runnable) {
            runnable = RunnableWrapper.get(runnable);
        }
    }

    @SuppressWarnings("unused")
    public static class ExecutorCallableAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(
                @Advice.Argument(value = 0, readOnly = false, optional = true) Callable callable) {
            callable = CallableWrapper.get(callable);
        }
    }
}

