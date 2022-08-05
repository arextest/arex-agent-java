package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateControl;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * AppClassLoaderInstrumentation
 *
 *
 * @date 2022/03/29
 */
public class AppClassLoaderInstrumentation extends TypeInstrumentation {
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
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(value = 0, readOnly = false) String name,
                                      @Advice.This ClassLoader thisClassLoader) {
            if (DecorateControl.forClass(ClassLoader.class).hasDecorated()) {
                return name.startsWith("io.arex.inst.");
            }

            ClassLoaderUtil.registerResource(thisClassLoader);
            return false;
        }
    }
}
