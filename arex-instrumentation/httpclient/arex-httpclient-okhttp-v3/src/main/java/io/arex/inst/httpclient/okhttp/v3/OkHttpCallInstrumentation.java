package io.arex.inst.httpclient.okhttp.v3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.util.IgnoreUtils;
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

    public static final class ExecuteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(
                @Advice.This Call call,
                @Advice.Local("wrapped") HttpClientExtractor<Request, Response> extractor,
                @Advice.Local("mockResult") MockResult mockResult) {
            Request request = call.request();
            if (IgnoreUtils.ignoreOperation(request.url().uri().getPath())) {
                return false;
            }

            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                OkHttpClientAdapter adapter = new OkHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = IOException.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Local("wrapped") HttpClientExtractor<Request, Response> extractor,
                @Advice.Thrown(readOnly = false) Exception throwable,
                @Advice.Return(readOnly = false) Response response,
                @Advice.Local("mockResult") MockResult mockResult) throws IOException {
            if (extractor == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult() && response == null) {
                if (mockResult.getThrowable() != null) {
                    throwable = (Exception) mockResult.getThrowable();
                } else {
                    // noinspection resource
                    response = (Response) mockResult.getResult();
                }
            }
            if (ContextManager.needRecord()) {
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(response);
                }
            }
        }
    }

    public static final class EnqueueAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.This Call call,
            @Advice.Argument(value = 0, readOnly = false) Callback callback,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (IgnoreUtils.ignoreOperation(call.request().url().uri().getPath())) {
                return false;
            }

            if (ContextManager.needRecordOrReplay() && RepeatedCollectManager.validate()) {
                // recording works in callback wrapper
                callback = new OkHttpCallbackWrapper(call, callback);
                if (ContextManager.needReplay()) {
                    mockResult = ((OkHttpCallbackWrapper) callback).replay();
                    return mockResult != null && mockResult.notIgnoreMockResult();
                }
            }
            return false;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(value = 0) Callback callback,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (callback instanceof OkHttpCallbackWrapper &&
                mockResult != null && mockResult.notIgnoreMockResult()) {
                OkHttpCallbackWrapper callbackWrapper = (OkHttpCallbackWrapper) callback;
                callbackWrapper.replay(mockResult);
            }
        }
    }
}