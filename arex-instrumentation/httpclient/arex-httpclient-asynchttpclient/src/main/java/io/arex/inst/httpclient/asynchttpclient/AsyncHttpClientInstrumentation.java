package io.arex.inst.httpclient.asynchttpclient;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.asynchttpclient.listener.AsyncHttpClientConsumer;
import io.arex.inst.httpclient.asynchttpclient.listener.AsyncHttpClientListenableFuture;
import io.arex.inst.httpclient.asynchttpclient.wrapper.AsyncHandlerWrapper;
import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseWrapper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.IgnoreUtils;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.OnNonDefaultValue;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AsyncHttpClientInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.asynchttpclient.DefaultAsyncHttpClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(named("execute").and(takesArguments(2))
            .and(takesArgument(0, named("org.asynchttpclient.Request"))), ExecuteAdvice.class.getName()));
    }

    public static class ExecuteAdvice {
        @OnMethodEnter(skipOn = OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Argument(0) Request request,
                @Argument(value = 1, readOnly = false) AsyncHandler<?> handler,
                @Local("extractor") AsyncHttpClientExtractor extractor,
                @Local("mockResult") MockResult mockResult) {
            if (IgnoreUtils.excludeOperation(request.getUri().getPath())) {
                return false;
            }

            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }

            if (ContextManager.needRecordOrReplay()) {
                ResponseWrapper response = new ResponseWrapper();
                extractor = new AsyncHttpClientExtractor(request, response);
                if (ContextManager.needReplay()) {
                   mockResult = extractor.replay();
                   return mockResult != null && mockResult.notIgnoreMockResult();
                }
                handler = new AsyncHandlerWrapper(handler, response);
            }
            return false;
        }

        @OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Argument(1) AsyncHandler<?> handler,
                                  @Return(readOnly = false, typing = Typing.DYNAMIC) ListenableFuture future,
                                  @Local("extractor") AsyncHttpClientExtractor extractor,
                                  @Local("mockResult") MockResult mockResult) {
            if (extractor == null) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
               if (mockResult.getThrowable() != null) {
                   future = new AsyncHttpClientListenableFuture(null, mockResult.getThrowable(), handler);
               } else {
                   future = new AsyncHttpClientListenableFuture(mockResult.getResult(), null, handler);
               }
               return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                future.toCompletableFuture().whenComplete(new AsyncHttpClientConsumer(extractor));
            }
        }

    }
}
