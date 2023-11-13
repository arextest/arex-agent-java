package io.arex.inst.httpclient.feign;

import feign.Request;
import feign.Response;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.IgnoreUtils;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.OnNonDefaultValue;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class FeignClientInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("feign.Client")).and(not(isInterface()));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(
            named("execute").and(takesArguments(2))
                .and(takesArgument(0, named("feign.Request"))),
            ExecuteAdvice.class.getName()));
    }

    public static class ExecuteAdvice{
        @OnMethodEnter(skipOn = OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Argument(0)Request request,
                @Local("adapter") FeignClientAdapter adapter,
                @Local("extractor") HttpClientExtractor extractor,
                @Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                final URI uri = URI.create(request.url());
                if (IgnoreUtils.excludeOperation(uri.getPath())) {
                    return false;
                }
                RepeatedCollectManager.enter();
                adapter = new FeignClientAdapter(request, uri);
                extractor = new HttpClientExtractor(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                    return mockResult != null && mockResult.notIgnoreMockResult();
                }
            }
            return false;
        }

        @OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Local("adapter") FeignClientAdapter adapter,
                @Local("extractor") HttpClientExtractor extractor,
                @Local("mockResult") MockResult mockResult,
                @Return(readOnly = false, typing = Typing.DYNAMIC) Response response,
                @Advice.Thrown(readOnly = false) Throwable throwable){
            if (extractor == null) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    response = (Response) mockResult.getResult();
                }
                return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                response = adapter.copyResponse(response);
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(response);
                }
            }
        }
    }
}
