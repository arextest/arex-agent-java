package io.arex.agent.bootstrap.cache;

import net.bytebuddy.dynamic.loading.ClassInjector;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdviceInjectorCache {

    public static ConcurrentHashMap<String, AdviceClassInjector> injectorMap = new ConcurrentHashMap<>(100);

    private static ConcurrentHashMap<String, Class<?>> clazzCache = new ConcurrentHashMap<>(100);

    public static void registerInjector(String name, AdviceClassInjector injector) {
        injectorMap.put(name, injector);
    }

    public static boolean contains(String name) {
        return injectorMap.containsKey(name);
    }

    public static Class<?> getAdviceClass(String name, ClassLoader loader) {
        Class<?> clazz = clazzCache.get(name);
        if (clazz != null) {
            return clazz;
        }

        AdviceClassInjector injector = injectorMap.get(name);
        if (injector == null) {
            return null;
        }

        clazz = injector.inject(loader, name);
        if (clazz != null) {
            clazzCache.put(name, clazz);
            injectorMap.remove(name);
        }

        return clazz;
    }

    public static class AdviceClassInjector {
        private final byte[] bytes;

        public AdviceClassInjector(byte[] bytes) {
            this.bytes = bytes;
        }

        Class<?> inject(ClassLoader classLoader, String className) {
            Map<String, Class<?>> result = new ClassInjector.UsingReflection(classLoader)
                            .injectRaw(Collections.singletonMap(className, bytes));
            return result.get(className);
        }
    }
}
