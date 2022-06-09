package io.arex.inst.httpclient.apache.sync;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class InternalHttpClientInstrumentation extends TypeInstrumentation {
    public InternalHttpClientInstrumentation(ModuleDescription module) {
        super(module);
    }

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

    public static class ExecuteAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(
                @Advice.Argument(1) HttpRequest request,
                @Advice.Local("wrapped") HttpClientExtractor<HttpRequest, HttpResponse> extractor) {
            if (ContextManager.needRecordOrReplay()) {
                HttpClientAdapter<HttpRequest, HttpResponse> adapter = new ApacheHttpClientAdapter(request);
                extractor = new HttpClientExtractor<>(adapter);
            }

            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(onThrowable = ClientProtocolException.class)
        public static void onExit(
                @Advice.Local("wrapped") HttpClientExtractor<HttpRequest, HttpResponse> extractor,
                @Advice.Thrown(readOnly = false) Exception throwable,
                @Advice.Return(readOnly = false) CloseableHttpResponse response) throws IOException {
            if (extractor == null) {
                return;
            }

            if (ContextManager.needReplay() && response == null) {
                response = (CloseableHttpResponse) extractor.replay();
                return;
            }

            if (throwable != null) {
                extractor.record(throwable);
            } else {
                extractor.record(response);
            }
        }
    }
}