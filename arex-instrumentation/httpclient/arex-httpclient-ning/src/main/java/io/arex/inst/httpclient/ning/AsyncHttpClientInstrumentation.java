package io.arex.inst.httpclient.ning;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.listener.DirectExecutor;
import io.arex.inst.runtime.util.IgnoreUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AsyncHttpClientInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ning.http.client.AsyncHttpClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(
                isMethod().and(named("executeRequest").and(takesArguments(2))), ExecuteRequestAdvice.class.getName()));
    }

    public static class ExecuteRequestAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class, skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Argument(0) Request request,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.Local("extractor") HttpClientExtractor<Request, Object> extractor){
            if (IgnoreUtils.excludeOperation(request.getUri().getPath())) {
                return false;
            }
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needRecordOrReplay()) {
                HttpClientAdapter<Request, Object> adapter = new NingHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                    return mockResult != null && mockResult.notIgnoreMockResult();
                }
            }
            return false;
        }

        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
        public static void onExit(@Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Local("extractor") HttpClientExtractor<Request, Object> extractor,
                                  @Advice.Return(readOnly = false) ListenableFuture responseFuture,
                                  @Advice.Thrown(readOnly = false) Throwable throwable) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    responseFuture = new ResponseFutureWrapper(mockResult.getResult());
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate("ning http client repeat record") && extractor != null) {
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    responseFuture.addListener(new ResponseFutureListener(extractor, responseFuture), DirectExecutor.INSTANCE);
                }
            }
        }
    }
}
