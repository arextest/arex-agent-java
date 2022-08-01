package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.matcher.HasSuperTypeMatcher;
import io.arex.foundation.util.StringUtil;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * LaunchedClassLoaderInstrumentation
 *
 * <p/>
 * This class mainly to solve the problems in the following link
 * <a href="https://github.com/raphw/byte-buddy/issues/87">
 *     Unable to instrument apache httpclient using javaagent for spring boot uber jar application
 * </a>
 *
 * @date 2022/03/29
 */
public class ClassLoaderInstrumentation extends TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.boot.loader.LaunchedURLClassLoader");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(isConstructor().and(takesArguments(4))
                .and(takesArgument(2, URL[].class)).and(takesArgument(3, ClassLoader.class)),
                        this.getClass().getName() + "$ConstructorAdvice"));
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 2, readOnly = false) URL[] urls,
                                   @Advice.Local("needRegister") Boolean needRegister) {
            int size = urls.length;
            String agentJarPath = System.getProperty("arex-agent-jar-file-path");
            urls = ClassLoaderUtil.addUcp(agentJarPath, urls);
            needRegister = size != urls.length;

            DecorateOnlyOnce.forClass(ClassLoader.class).setDecorated();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.This ClassLoader classLoader,
                                  @Advice.Local("needRegister") Boolean needRegister) {
            if (needRegister) {
                ClassLoaderUtil.registerResource(classLoader);
                DecorateOnlyOnce.forClass(ClassLoader.class).setDecorated();
            }
        }
    }

}
