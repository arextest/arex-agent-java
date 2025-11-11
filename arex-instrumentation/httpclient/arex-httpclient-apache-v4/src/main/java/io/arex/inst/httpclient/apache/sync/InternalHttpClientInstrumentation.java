package io.arex.inst.httpclient.apache.sync;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.apache.async.NextBuilderCallbackWrapper;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientHelper;
import io.arex.inst.httpclient.apache.common.NextBuilderExtractor;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

public class InternalHttpClientInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("org.apache.http.impl.client.InternalHttpClient",
            "org.apache.http.impl.client.MinimalHttpClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
            isMethod().and(named("doExecute"))
                .and(takesArguments(3))
                .and(takesArgument(0, named("org.apache.http.HttpHost")))
                .and(takesArgument(1, named("org.apache.http.HttpRequest")))
                .and(takesArgument(2, named("org.apache.http.protocol.HttpContext"))),
            this.getClass().getName() + "$ExecuteAdvice"));
    }

    public static class ExecuteAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(
            @Advice.Argument(1) HttpRequest request,
            @Advice.Local("extractor") HttpClientExtractor<HttpRequest, HttpResponse> extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (ApacheHttpClientHelper.ignoreRequest(request)) {
                return false;
            }

            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                HttpClientAdapter<HttpRequest, HttpResponse> adapter = new ApacheHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            } else if (NextBuilderCallbackWrapper.openMock()) {
                HttpClientAdapter<HttpRequest, HttpResponse> adapter = new ApacheHttpClientAdapter(request);
                mockResult = new NextBuilderExtractor<>(adapter).mock();
            }

            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Exception.class, suppress = Throwable.class)
        public static void onExit(
            @Advice.Thrown(readOnly = false) Exception throwable,
            @Advice.Return(readOnly = false) CloseableHttpResponse response,
            @Advice.Local("extractor") HttpClientExtractor<HttpRequest, HttpResponse> extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (mockResult != null && mockResult.notIgnoreMockResult() && response == null) {
                if (mockResult.getThrowable() != null) {
                    throwable = (Exception) mockResult.getThrowable();
                } else {
                    response = (CloseableHttpResponse) mockResult.getResult();
                }
                return;
            }
            if (extractor == null) {
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(response);
                }
            }
        }
    }
}
