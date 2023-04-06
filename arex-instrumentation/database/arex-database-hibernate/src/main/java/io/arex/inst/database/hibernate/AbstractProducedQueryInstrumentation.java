package io.arex.inst.database.hibernate;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * AbstractProducedQueryInstrumentation
 *
 *
 * @date 2022/03/16
 */
public class AbstractProducedQueryInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.hibernate.query.internal.AbstractProducedQuery");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher =
            isPublic().and(isStatic()).and(isMethod()).and(named("uniqueElement")).and(takesArgument(0, List.class));

        String adviceClassName = this.getClass().getName() + "$UniqueElementAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    private static class UniqueElementAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(0) List list) {
            if (!ContextManager.needReplay()) {
                return;
            }

            if (list.size() == 0) {
                return;
            }

            // Restore the list when getSingleResult
            if (list.size() == 2) {
                Object first = list.get(0);
                list.remove(1);
                list.add(first);
            }
        }
    }
}
