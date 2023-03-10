package io.arex.inst.authentication.jcasbin;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * JCasbinInstrumentation
 */
public class JCasbinInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.casbin.jcasbin.main.CoreEnforcer");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("enforce").and(isPublic())
                .and(takesArgument(0, named("java.lang.Object[]")));

        String adviceClassName = this.getClass().getName() + "$MethodAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class MethodAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Enter boolean needReplay, @Advice.Return(readOnly = false) boolean result) {
            if (needReplay) {
                result = true;
            }
        }
    }
}
