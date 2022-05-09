package io.arex.inst.apache.httpclient.sync;

import io.arex.api.instrumentation.MethodInstrumentation;
import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.TypeInstrumentation;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.inst.apache.httpclient.common.ApacheClientExtractor;
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
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

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
                        .and(takesArgument(1,named("org.apache.http.HttpRequest")))
                        .and(takesArgument(2, named("org.apache.http.protocol.HttpContext"))),
                this.getClass().getName() + "$ExecuteAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ExecuteAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(
            @Advice.Argument(1) HttpRequest request,
            @Advice.Local("wrapped") ApacheClientExtractor extractor) {
            ArexContext context = ContextManager.currentContext();
            if (context != null) {
                boolean needReplay = context.isReplay() && extractor.isMockEnabled();
                if (needReplay) {
                    extractor = new ApacheClientExtractor(request);
                }
            }

            return false;
        }

        @Advice.OnMethodExit(onThrowable = ClientProtocolException.class)
        public static void onExit(
                @Advice.Local("wrapped") ApacheClientExtractor extractor,
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
