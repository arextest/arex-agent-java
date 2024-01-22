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

/**
 * ShiroDelegatingSubjectInstrumentation
 */
public class ShiroDelegatingSubjectInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.shiro.subject.support.DelegatingSubject");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation assertAuthorizedMethod = new MethodInstrumentation(
                named("assertAuthzCheckPossible"),
                AssertAuthzCheckPossibleAdvice.class.getName());
        return asList(assertAuthorizedMethod);
    }

    public static class AssertAuthzCheckPossibleAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }
    }
}
