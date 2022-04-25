package io.arex.inst.apache.httpclient.async;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.util.List;
import java.util.concurrent.Future;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class InternalHttpAsyncClientInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.nio.client.InternalHttpAsyncClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("execute"))
                        .and(takesArguments(4))
                        .and(takesArgument(0, named("org.apache.http.nio.protocol.HttpAsyncRequestProducer")))
                        .and(takesArgument(1,named("org.apache.http.nio.protocol.HttpAsyncResponseConsumer")))
                        .and(takesArgument(2, named("org.apache.http.protocol.HttpContext")))
                        .and(takesArgument(3, named("org.apache.http.concurrent.FutureCallback"))),
                this.getClass().getName() + "$ExecuteAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ExecuteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(
                @Advice.Argument(0) HttpAsyncRequestProducer producer,
                @Advice.Argument(value = 3, readOnly = false) FutureCallback<?> callback,
                @Advice.Local("wrapped") FutureCallbackWrapper wrapped) {
            ArexContext context = ContextManager.currentContext();
            if (context != null) {
                boolean needReplay = context.isReplay() && wrapped.isMockEnabled();
                if (needReplay) {
                    wrapped = FutureCallbackWrapper.get(producer, callback);
                    callback = wrapped;
                }
            }

            return false;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.Local("wrapped") FutureCallbackWrapper wrapped,
                @Advice.Return(readOnly = false) Future<?> future) {
            if (wrapped == null) {
                return;
            }

            if (ContextManager.needReplay()) {
                wrapped.replay();
                future = new BasicFuture<>(wrapped);
            }
            // recording works in FutureCallbackWrapper
        }
    }
}
