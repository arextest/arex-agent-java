package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.runtime.context.ResourceManager;
import io.arex.inst.extension.ModuleDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.WeakHashMap;

public class ModuleVersionMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {
    public static ElementMatcher.Junction<ClassLoader> versionMatch(ModuleDescription description) {
        return new IgnoreClassloaderMatcher(new ModuleVersionMatcher(description));
    }

    private final Map<ClassLoader, Boolean> cache = new WeakHashMap<>();
    private final ModuleDescription description;

    ModuleVersionMatcher(ModuleDescription description) {
        this.description = description;
    }

    @Override
    public boolean matches(ClassLoader cl) {
        if (cl == null || cl.getClass().getName().startsWith("sun.reflect.DelegatingClassLoader")) {
            return false;
        }
        return description == null || cache.computeIfAbsent(cl, this::versionMatches);
    }

    private boolean versionMatches(ClassLoader loader) {
        ResourceManager.registerResources(loader);
        Pair<Integer, Integer> version = LoadedModuleCache.get(description.getModuleName());
        if (version == null) {
            return true;
        }
        return description.isSupported(version);
    }

    static class IgnoreClassloaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {
        private ElementMatcher<ClassLoader> matcher;

        public IgnoreClassloaderMatcher(ElementMatcher<ClassLoader> matcher) {
            this.matcher = matcher;
        }

        @Override
        public boolean matches(ClassLoader loader) {
            if (loader == null) {
                return false;
            }

            String loaderName = loader.getClass().getName();
            if (loaderName.startsWith("sun.reflect") || loaderName.startsWith("jdk.internal.reflect")) {
                return false;
            }
            return matcher.matches(loader);
        }
    }
}
