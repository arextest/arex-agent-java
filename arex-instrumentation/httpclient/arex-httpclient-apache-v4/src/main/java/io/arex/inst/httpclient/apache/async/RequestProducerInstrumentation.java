package io.arex.inst.httpclient.apache.async;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.runtime.context.ContextManager;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpRequest;

/**
 * Wrapped the http request for reading request body repeatedly.
 * @since 2024/2/21
 */
public class RequestProducerInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.nio.protocol.BasicAsyncRequestProducer");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isConstructor().and(takesArguments(2))
                    .and(takesArgument(0, named("org.apache.http.HttpHost")))
                    .and(takesArgument(1, named("org.apache.http.HttpRequest"))),
            ConstructorAdvice.class.getName()));
    }

    static class ConstructorAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(1) HttpRequest request) {
            if (ContextManager.needRecordOrReplay()) {
                ApacheHttpClientAdapter.wrapHttpEntity(request);
            }
        }
    }
}
