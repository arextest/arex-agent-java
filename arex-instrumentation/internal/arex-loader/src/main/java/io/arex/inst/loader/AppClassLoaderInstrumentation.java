package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.api.instrumentation.MethodInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.InputStream;
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
                                      @Advice.FieldValue("ucp") URLClassPath ucp) {
            if (DecorateOnlyOnce.forClass(ClassLoader.class).hasDecorated()) {
                return name.startsWith("io.arex.inst.");
            }

            DecorateOnlyOnce call = DecorateOnlyOnce.forClass(URLClassLoader.class);
            if (call.hasDecorated()) {
                return false;
            }
            call.setDecorated();

            Enumeration<Resource> files = ucp.getResources("META-INF/MANIFEST.MF");
            while (files.hasMoreElements()) {
                try (InputStream inputStream = files.nextElement().getInputStream()) {
                    Manifest mf = new Manifest(inputStream);
                    String packageName = mf.getMainAttributes().getValue("Bundle-Name");
                    if (packageName == null || "".equals(packageName)) {
                        packageName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                    }
                    if (packageName == null || "".equals(packageName)) {
                        continue;
                    }

                    String version = mf.getMainAttributes().getValue("Bundle-Version");
                    if (version == null || "".equals(version)) {
                        version = mf.getMainAttributes().getValue("Implementation-Version");
                    }
                    if (version == null || "".equals(version)) {
                        continue;
                    }
                    LoadedModuleCache.registerResource(packageName, version);
                } catch (Exception e) {
                    continue;
                }
            }
            return false;
        }
    }
}
