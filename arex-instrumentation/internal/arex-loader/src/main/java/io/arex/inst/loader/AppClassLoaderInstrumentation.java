package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * AppClassLoaderInstrumentation
 *
 *
 * @date 2022/03/29
 */
public class AppClassLoaderInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("sun.misc.Launcher$AppClassLoader").or(named("jdk.internal.loader.ClassLoaders$AppClassLoader"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(
            new MethodInstrumentation(
                isMethod().and(named("loadClass").and(takesArguments(2))),
                this.getClass().getName() + "$LoadClassAdvice"));
    }

    @SuppressWarnings("unused")
    public static class LoadClassAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Argument(value = 0, readOnly = false) String name) {
            DecorateOnlyOnce call = DecorateOnlyOnce.forClass(ClassLoader.class);
            if (call.hasDecorated()) {
                return name.startsWith("io.arex.inst.");
            }
            return false;
        }
    }
}
