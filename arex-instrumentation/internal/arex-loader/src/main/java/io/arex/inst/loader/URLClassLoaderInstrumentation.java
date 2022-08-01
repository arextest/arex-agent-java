package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import sun.misc.URLClassPath;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class URLClassLoaderInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("java.net.URLClassLoader");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(
                new MethodInstrumentation(isMethod().and(named("findClass")),
                        this.getClass().getName() + "$FindClassAdvice"));
    }


    @SuppressWarnings("unused")
    public static class FindClassAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This ClassLoader classLoader,
                                   @Advice.Argument(value = 0) String name,
                                   @Advice.FieldValue("ucp") URLClassPath ucp) {
            if (!DecorateOnlyOnce.forClass(URLClassLoader.class).hasDecorated() ||
                    DecorateOnlyOnce.forClass(URLClassPath.class).hasDecorated()) {
                return;
            }

            if (name.startsWith("io.arex.inst")) {
                URL url;
                try {
                    url = new File(System.getProperty("arex-agent-jar-file-path")).toURI().toURL();
                    ucp.addURL(url);
                    DecorateOnlyOnce.forClass(URLClassPath.class).setDecorated();

                } catch (IOException e) {
                }
                System.out.println("[AREX] URL ClassLoader load class:" + name + ",classloader:" + Thread.currentThread().getContextClassLoader());
            }
        }
    }
}
