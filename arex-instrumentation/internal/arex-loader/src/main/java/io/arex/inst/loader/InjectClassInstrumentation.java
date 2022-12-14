package io.arex.inst.loader;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static io.arex.inst.extension.matcher.SafeExtendsClassMatcher.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;

public class InjectClassInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("java.lang.ClassLoader"))
                .and(not(namedOneOf("io.arex.agent.bootstrap.AgentClassLoader",
                        "sun.misc.Launcher$AppClassLoader", "jdk.internal.loader.BuiltinClassLoader")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(
                isMethod().and(named("loadClass"))
                        .and(takesArguments(1)
                            .and(takesArgument(0, String.class))
                        .or(takesArguments(2)
                            .and(takesArgument(0, String.class))
                            .and(takesArgument(1, boolean.class))))
                        .and(isPublic().or(isProtected()))
                        .and(not(isStatic())),
                InjectClassInstrumentation.class.getName() + "$LoadClassAdvice"
        ));
    }

    @SuppressWarnings("unused")
    public static class LoadClassAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static Class<?> onEnter(
                @Advice.This ClassLoader classLoader, @Advice.Argument(0) String name) {
            Class<?> adviceClass = AdviceInjectorCache.getAdviceClass(name, classLoader);
            if (adviceClass != null) {
                return adviceClass;
            }

            return null;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(
                @Advice.Return(readOnly = false) Class<?> result, @Advice.Enter Class<?> loadedClass) {
            if (loadedClass != null) {
                result = loadedClass;
            }
        }
    }
}
