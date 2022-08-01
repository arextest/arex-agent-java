package io.arex.inst.loader;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class WebAppClassLoaderBaseInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.loader.WebappClassLoaderBase");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(isMethod().and(named("start")),
                        this.getClass().getName() + "$StartAdvice"));
    }

    @SuppressWarnings("unused")
    public static class StartAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This ClassLoader classLoader,
                                  @Advice.Local("needRegister") Boolean needRegister) {
            ClassLoaderUtil.registerResource(classLoader);
        }
    }
}
