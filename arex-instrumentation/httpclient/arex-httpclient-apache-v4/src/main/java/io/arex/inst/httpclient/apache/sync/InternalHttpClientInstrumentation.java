package io.arex.inst.httpclient.apache.sync;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.foundation.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.List;

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
                ExecuteAdvice.class.getName()));
    }

    @Override
    public List<String> adviceClassNames() {
        return singletonList(
                "io.arex.inst.httpclient.apache.sync.InternalHttpClientInstrumentation$ExecuteAdvice");
    }

    public static class ExecuteAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(
                @Advice.Argument(1) HttpRequest request,
                @Advice.Local("extractor") HttpClientExtractor<HttpRequest, MockResult> extractor,
                @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                HttpClientAdapter<HttpRequest, MockResult> adapter = new ApacheHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }

            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = ClientProtocolException.class)
        public static void onExit(
                @Advice.Local("extractor") HttpClientExtractor<HttpRequest, MockResult> extractor,
                @Advice.Thrown(readOnly = false) Exception throwable,
                @Advice.Return(readOnly = false) CloseableHttpResponse response,
                @Advice.Local("mockResult") MockResult mockResult) throws IOException {
            if (extractor == null) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult() && response == null) {
                response = (CloseableHttpResponse) mockResult.getMockResult();
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(MockResult.of(response));
                }
            }
        }
    }
}