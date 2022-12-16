package io.arex.inst.httpclient.apache.async;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnNonDefaultValue;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.util.List;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class InternalHttpAsyncClientInstrumentation extends TypeInstrumentation {

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
                        .and(takesArgument(1, named("org.apache.http.nio.protocol.HttpAsyncResponseConsumer")))
                        .and(takesArgument(2, named("org.apache.http.protocol.HttpContext")))
                        .and(takesArgument(3, named("org.apache.http.concurrent.FutureCallback"))),
                this.getClass().getName() + "$ExecuteAdvice"));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpclient.apache.async.FutureCallbackWrapper",
                "io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter",
                "io.arex.inst.httpclient.apache.common.ApacheHttpClientHelper",
                "io.arex.inst.httpclient.apache.common.CloseableHttpResponseProxy",
                "io.arex.inst.httpclient.common.ArexDataException",
                "io.arex.inst.httpclient.common.ExceptionWrapper",
                "io.arex.inst.httpclient.common.HttpClientExtractor",
                "io.arex.inst.httpclient.common.HttpClientAdapter",
                "io.arex.inst.httpclient.common.HttpResponseWrapper",
                "io.arex.inst.httpclient.common.HttpResponseWrapper$StringTuple");
    }

    @SuppressWarnings("unused")
    public static class ExecuteAdvice {
        @OnMethodEnter(skipOn = OnNonDefaultValue.class)
        public static boolean onEnter(@Argument(0) HttpAsyncRequestProducer producer,
                                      @Argument(value = 3, readOnly = false) FutureCallback<?> callback,
                                      @Local("wrapped") FutureCallbackWrapper<?> wrapped) {
            if (ContextManager.needRecordOrReplay() && RepeatedCollectManager.validate()) {
                wrapped = FutureCallbackWrapper.get(producer, callback);
                if (wrapped != null) {
                    callback = wrapped;
                    return ContextManager.needReplay();
                }
            }
            return false;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Local("wrapped") FutureCallbackWrapper<?> wrapped,
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