package io.arex.inst.httpclient.apache.async;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
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
                "io.arex.inst.httpclient.common.HttpClientExtractor",
                "io.arex.inst.httpclient.common.HttpClientAdapter",
                "io.arex.inst.httpclient.common.HttpResponseWrapper",
                "io.arex.inst.httpclient.common.HttpResponseWrapper$StringTuple");
    }

    @SuppressWarnings("unused")
    public static class ExecuteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Argument(0) HttpAsyncRequestProducer producer,
            @Advice.Argument(value = 3, readOnly = false) FutureCallback<?> callback,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay() && RepeatedCollectManager.validate()) {
                // recording works in callback wrapper
                FutureCallbackWrapper<?> callbackWrapper = FutureCallbackWrapper.get(producer, callback);
                if (callbackWrapper != null) {
                    callback = callbackWrapper;
                    if (ContextManager.needReplay()) {
                        mockResult = ((FutureCallbackWrapper<?>)callback).replay();
                        return mockResult != null && mockResult.notIgnoreMockResult();
                    }
                }
            } else {
                callback = FutureCallbackWrapper.wrap(callback);
            }
            return false;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(value = 3, readOnly = false) FutureCallback<?> callback,
            @Advice.Return(readOnly = false) Future<?> future,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (callback instanceof FutureCallbackWrapper &&
                mockResult != null && mockResult.notIgnoreMockResult()) {
                FutureCallbackWrapper<?> callbackWrapper = (FutureCallbackWrapper<?>) callback;
                future = callbackWrapper.replay(mockResult);
            }
        }
    }
}