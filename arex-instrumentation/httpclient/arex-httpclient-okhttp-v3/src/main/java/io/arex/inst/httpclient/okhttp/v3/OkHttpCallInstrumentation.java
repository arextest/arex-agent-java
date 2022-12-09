package io.arex.inst.httpclient.okhttp.v3;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

public class OkHttpCallInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("okhttp3.internal.connection.RealCall");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation executeMethod = new MethodInstrumentation(
                named("execute").and(takesNoArguments()),
                ExecuteAdvice.class.getName());
        MethodInstrumentation enqueueMethod = new MethodInstrumentation(
                named("enqueue").and(takesArguments(1)),
                EnqueueAdvice.class.getName());
        return asList(executeMethod, enqueueMethod);

    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpclient.okhttp.v3.OkHttpCallInstrumentation$ExecuteAdvice",
                "io.arex.inst.httpclient.okhttp.v3.OkHttpClientAdapter",
                "io.arex.inst.httpclient.common.HttpClientAdapter",
                "io.arex.inst.httpclient.common.HttpResponseWrapper",
                "io.arex.inst.httpclient.common.HttpResponseWrapper$StringTuple",
                "io.arex.inst.httpclient.common.ArexDataException",
                "io.arex.inst.httpclient.common.ExceptionWrapper",
                "io.arex.inst.httpclient.okhttp.v3.OkHttpCallbackWrapper",
                "io.arex.inst.httpclient.common.HttpClientExtractor");
    }

    public static final class ExecuteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(
                @Advice.This Call call,
                @Advice.Local("wrapped") HttpClientExtractor<Request, MockResult> extractor,
                @Advice.Local("mockResult") MockResult mockResult) {
            OkHttpClientAdapter adapter;
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                adapter = new OkHttpClientAdapter(call.request().newBuilder().build());
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = IOException.class)
        public static void onExit(
                @Advice.Local("wrapped") HttpClientExtractor<Request, MockResult> extractor,
                @Advice.Thrown(readOnly = false) Exception throwable,
                @Advice.Return(readOnly = false) Response response,
                @Advice.Local("mockResult") MockResult mockResult) throws IOException {
            if (extractor == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult() && response == null) {
                // noinspection resource
                response = (Response) mockResult.getResult();
                return;
            }
            if (ContextManager.needRecord()) {
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(MockResult.success(response));
                }
            }
        }
    }

    public static final class EnqueueAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(
                @Advice.This Call call,
                @Advice.Argument(value = 0, readOnly = false) Callback callback) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                callback = new OkHttpCallbackWrapper(call, callback);
            }
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(value = 0) Callback callback) {
            OkHttpCallbackWrapper okHttpCallbackWrapper;
            if (!(callback instanceof OkHttpCallbackWrapper)) {
                return;
            }
            okHttpCallbackWrapper = (OkHttpCallbackWrapper) callback;
            if (ContextManager.needReplay()) {
                okHttpCallbackWrapper.replay();
            }
        }

    }
}