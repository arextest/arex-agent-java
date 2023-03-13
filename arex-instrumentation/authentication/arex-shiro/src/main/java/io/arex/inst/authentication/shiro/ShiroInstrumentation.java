package io.arex.inst.authentication.shiro;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * ShiroInstrumentation
 */
public class ShiroInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.shiro.web.filter.PathMatchingFilter");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation attemptAuthorizationMethod = new MethodInstrumentation(
                named("preHandle").and(takesArguments(2)),
                PreHandleAdvice.class.getName());
        return asList(attemptAuthorizationMethod);
    }

    public static class PreHandleAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit()
        public static void onExit(@Advice.Enter boolean needReplay,
                                  @Advice.Return(readOnly = false) boolean result) {
            if (needReplay) {
                result = true;
            }
        }
    }
}
