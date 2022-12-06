package io.arex.foundation.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.internal.ResourceManager;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.WeakHashMap;

public class ModuleVersionMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {
    public static ElementMatcher.Junction<ClassLoader> versionMatch(ModuleDescription description) {
        return new ModuleVersionMatcher(description);
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
        if (loader == null) {
            return false;
        }

        ResourceManager.registerResources(loader);
        Pair<Integer, Integer> version = LoadedModuleCache.get(description.getModuleName());
        if (version == null) {
            return true;
        }
        return description.isSupported(version);
    }
}
