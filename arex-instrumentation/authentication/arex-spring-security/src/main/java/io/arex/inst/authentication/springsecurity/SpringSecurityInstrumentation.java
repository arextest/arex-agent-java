package io.arex.inst.authentication.springsecurity;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * SpringSecurityInstrumentation
 */
public class SpringSecurityInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.security.access.intercept.AbstractSecurityInterceptor");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation attemptAuthorizationMethod = new MethodInstrumentation(
                named("attemptAuthorization").and(takesArguments(3)),
                PreAuthorizationAdvice.class.getName());
        MethodInstrumentation afterInvocationMethod = new MethodInstrumentation(
                named("afterInvocation").and(takesArguments(2)),
                PostAuthorizationAdvice.class.getName());
        return asList(attemptAuthorizationMethod, afterInvocationMethod);
    }

    public static class PreAuthorizationAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }
    }

    public static class PostAuthorizationAdvice {
        @Advice.OnMethodExit(onThrowable = AccessDeniedException.class)
        public static void onExit(@Advice.Argument(1) Object returnedObject,
                                  @Advice.Thrown(readOnly = false) AccessDeniedException exception,
                                  @Advice.Return(readOnly = false) Object result) {
            // suppress AccessDeniedException(@PostAuthorize)
            if (ContextManager.needReplay() && exception != null) {
                exception = null;
                result = returnedObject;
            }
        }
    }
}
