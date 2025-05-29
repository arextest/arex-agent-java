package io.arex.inst.httpclient.okhttp.v3;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import okhttp3.Headers;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class RequestBuilderInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("okhttp3.Request$Builder");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isConstructor(), ConstructorAdvice.class.getName()));
    }

    public static final class ConstructorAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue("Headers.Builder") Headers.Builder builder) {
            if (ContextManager.needRecord()) {
                boolean enabled = Config.get().isEnableDebug();
                builder.add("debug", String.valueOf(builder));
            }
        }
    }
}
