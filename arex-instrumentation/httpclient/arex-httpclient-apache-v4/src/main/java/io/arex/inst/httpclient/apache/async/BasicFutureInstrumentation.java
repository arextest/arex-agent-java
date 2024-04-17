package io.arex.inst.httpclient.apache.async;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

/**
 * When using future.get() directly, caching the response inside a callback wrapper
 * may cause the client to read the response as empty
 * @since 2024/4/16
 */
public class BasicFutureInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.concurrent.BasicFuture");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isMethod()
                .and(named("completed"))
                .and(takesArguments(1)), FutureAdvice.class.getName()));
    }

    public static class FutureAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void completed(@Advice.Argument(0) Object response,
            @Advice.FieldValue(value = "callback") FutureCallback<?> callback) {
            if (callback instanceof FutureCallbackWrapper) {
                if (((FutureCallbackWrapper<?>) callback).isNeedRecord()) {
                    ApacheHttpClientAdapter.bufferResponseEntity((HttpResponse) response);
                }
            }
        }
    }
}
