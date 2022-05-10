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
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

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
        return not(nameStartsWith("java.")
                    .or(nameStartsWith("sun.")))
                .and(HasSuperTypeMatcher.hasSuperType(named("java.lang.ClassLoader")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(
                // spring LaunchedURLClassLoader
                new MethodInstrumentation(isConstructor().and(takesArguments(4))
                        .and(takesArgument(2, URL[].class))
                        .and(takesArgument(3, ClassLoader.class)),
                        this.getClass().getName() + "$ConstructorAdvice"),
                // other user defined class loader, only support XxxClassLoader(URL[] urls, ClassLoader parent)
                new MethodInstrumentation(isConstructor().and(takesArguments(2))
                        .and(takesArgument(0, URL[].class))
                        .and(takesArgument(1, ClassLoader.class)),
                        this.getClass().getName() + "$Constructor2Advice"));
    }

    public static URL[] addUcp(String agentJarPath, URL[] urls) {
        if (urls == null || urls.length == 0) {
            return urls;
        }

        DecorateOnlyOnce call = DecorateOnlyOnce.forClass(ClassLoader.class);
        if (call.hasDecorated()) {
            // just decorate the top level class loader and only decorate once for all class loader
            return urls;
        }
        call.setDecorated();

        URL url;
        try {
            url = new File(agentJarPath).toURI().toURL();
        } catch (IOException e) {
            return urls;
        }

        URL[] newURLs = new URL[urls.length + 1];
        for (int i = 0; i < urls.length; i++) {
            newURLs[i] = urls[i];
        }
        newURLs[urls.length] = url;
        return newURLs;
    }

    public static void registerResource(ClassLoader classLoader) {
        Enumeration<URL> files;
        try {
            files = classLoader.getResources("META-INF/MANIFEST.MF");
        } catch (Exception ex) {
            return;
        }

        while (files.hasMoreElements()) {
            URL url = files.nextElement();
            try (InputStream stream = url.openStream()) {
                Manifest mf = new Manifest(stream);
                String packageName = mf.getMainAttributes().getValue("Bundle-Name");
                if (StringUtil.isEmpty(packageName)) {
                    packageName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                }
                if (StringUtil.isEmpty(packageName)) {
                    continue;
                }

                String version = mf.getMainAttributes().getValue("Bundle-Version");
                if (StringUtil.isEmpty(version)) {
                    version = mf.getMainAttributes().getValue("Implementation-Version");
                }
                if (StringUtil.isEmpty(version)) {
                    continue;
                }
                LoadedModuleCache.registerResource(packageName, version);
            } catch (Exception ex) {
                continue;
            }
        }
    }

    @SuppressWarnings("unused")
    public static class Constructor2Advice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) URL[] urls,
                                   @Advice.Argument(value = 1) ClassLoader parent,
                                   @Advice.Local("needRegister") Boolean needRegister) {
            int size = urls.length;
            String agentJarPath = System.getProperty("arex-agent-jar-file-path");
            urls = addUcp(agentJarPath, urls);
            needRegister = size != urls.length;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.This ClassLoader classLoader,
                                  @Advice.Local("needRegister") Boolean needRegister) {
            if (needRegister) {
                registerResource(classLoader);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class ConstructorAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 2, readOnly = false) URL[] urls,
                                   @Advice.Local("needRegister") Boolean needRegister) {
            int size = urls.length;
            String agentJarPath = System.getProperty("arex-agent-jar-file-path");
            urls = addUcp(agentJarPath, urls);
            needRegister = size != urls.length;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.This ClassLoader classLoader,
                                  @Advice.Local("needRegister") Boolean needRegister) {
            if (needRegister) {
                registerResource(classLoader);
            }
        }
    }

}
