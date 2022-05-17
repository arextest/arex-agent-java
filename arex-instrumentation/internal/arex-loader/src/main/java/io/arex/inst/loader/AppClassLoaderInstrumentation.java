package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

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
            if (DecorateOnlyOnce.forClass(ClassLoader.class).hasDecorated()) {
                return name.startsWith("io.arex.inst.");
            }

            DecorateOnlyOnce call = DecorateOnlyOnce.forClass(URLClassLoader.class);
            if (call.hasDecorated()) {
                return false;
            }
            call.setDecorated();

            try {
                Enumeration<URL> urls = thisClassLoader.getResources("META-INF/MANIFEST.MF");
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try (InputStream inputStream = url.openStream()){
                        Manifest manifest = new Manifest(inputStream);
                        String packageName = manifest.getMainAttributes().getValue("Bundle-Name");
                        if (packageName == null || packageName.equals("")) {
                            packageName = manifest.getMainAttributes().getValue("Automatic-Module-Name");
                        }
                        if (packageName == null || packageName.equals("")) {
                            continue;
                        }

                        String version = manifest.getMainAttributes().getValue("Bundle-Version");
                        if (version == null || version.equals("")) {
                            version = manifest.getMainAttributes().getValue("Implementation-Version");
                        }
                        if (version == null || version.equals("")) {
                            continue;
                        }
                        LoadedModuleCache.registerResource(packageName, version);
                    } catch (IOException e) {
                        continue;
                    }
                }
            } catch (IOException e) {
            }

            return false;
        }
    }
}
