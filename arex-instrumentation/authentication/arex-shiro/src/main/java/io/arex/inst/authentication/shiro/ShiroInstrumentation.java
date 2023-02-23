package io.arex.inst.authentication.shiro;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;

import javax.servlet.ServletRequest;
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
        public static boolean onEnter(@Advice.Argument(0) ServletRequest request) {
            boolean isReplay = false;
            if (request instanceof ShiroHttpServletRequest) {
                // authentication is through the filter chain and has not yet reached the servlet
                String recordId = ((ShiroHttpServletRequest)request).getHeader(ArexConstants.RECORD_ID);
                if (recordId != null && recordId.length() > 0) {
                    isReplay = true;
                }
            }
            return isReplay || ContextManager.needReplay();
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
