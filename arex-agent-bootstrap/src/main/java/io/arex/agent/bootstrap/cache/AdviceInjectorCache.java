package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.AgentClassLoader;
import io.arex.agent.bootstrap.internal.Cache;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.util.Collections;
import java.util.Map;

public class AdviceInjectorCache {

    public static Cache<String, AdviceClassInjector> injectorCache = Cache.trieCacheWithInit("io.arex.inst.");

    public static void registerInjector(String name, AdviceClassInjector injector) {
        injectorCache.put(name, injector);
    }

    public static boolean contains(String name) {
        return injectorCache.contains(name);
    }

    public static Class<?> getAdviceClass(String name, ClassLoader loader) {
        if (loader == null || injectorCache == null || loader instanceof AgentClassLoader) {
            return null;
        }

        AdviceClassInjector injector = injectorCache.get(name);
        return injector == null ? null : injector.inject(loader, name);
    }

    public static class AdviceClassInjector {
        private byte[] bytes;
        private Class<?> clazz;

        public AdviceClassInjector(byte[] bytes) {
            this.bytes = bytes;
        }

        Class<?> inject(ClassLoader classLoader, String className) {
            if (clazz == null) {
                Map<String, Class<?>> result = new ClassInjector.UsingReflection(classLoader)
                        .injectRaw(Collections.singletonMap(className, bytes));
                clazz = result.get(className);
                bytes = null;
            }
            return clazz;
        }
    }
}
