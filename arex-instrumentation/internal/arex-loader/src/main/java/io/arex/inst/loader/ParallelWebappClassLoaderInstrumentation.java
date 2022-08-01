package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ParallelWebappClassLoaderInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.loader.ParallelWebappClassLoader");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isConstructor(),
                this.getClass().getName() + "$ConstructorAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodExit
        public static void onExit() {
            DecorateOnlyOnce decorateOnlyOnce = DecorateOnlyOnce.forClass(ClassLoader.class);
            if (!decorateOnlyOnce.hasDecorated()) {
                decorateOnlyOnce.setDecorated();
                DecorateOnlyOnce.forClass(URLClassLoader.class).setDecorated();
            }
        }
    }
}
