package io.arex.inst.httpclient.apache.sync;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class InternalHttpClientInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.client.InternalHttpClient");
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

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpclient.common.HttpClientExtractor",
                "io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter",
                "io.arex.inst.httpclient.apache.common.ApacheHttpClientHelper",
                "io.arex.inst.httpclient.common.HttpClientAdapter",
                "io.arex.inst.httpclient.apache.common.CloseableHttpResponseProxy",
                "io.arex.inst.httpclient.common.HttpResponseWrapper",
                "io.arex.inst.httpclient.common.HttpResponseWrapper$StringTuple");
    }

    public static class ExecuteAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(
                @Advice.Argument(1) HttpRequest request,
                @Advice.Local("extractor") HttpClientExtractor<HttpRequest, HttpResponse> extractor,
                @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                HttpClientAdapter<HttpRequest, HttpResponse> adapter = new ApacheHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }

            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Exception.class)
        public static void onExit(
                @Advice.Thrown(readOnly = false) Exception throwable,
                @Advice.Return(readOnly = false) CloseableHttpResponse response,
                @Advice.Local("extractor") HttpClientExtractor<HttpRequest, HttpResponse> extractor,
                @Advice.Local("mockResult") MockResult mockResult) {
            if (extractor == null) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult() && response == null) {
                if (mockResult.getThrowable() != null) {
                    throwable = (Exception) mockResult.getThrowable();
                } else {
                    response = (CloseableHttpResponse) mockResult.getResult();
                }
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