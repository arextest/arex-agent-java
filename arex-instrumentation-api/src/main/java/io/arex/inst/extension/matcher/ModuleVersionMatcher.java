package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.ConcurrentCache;
import io.arex.inst.runtime.context.ResourceManager;
import io.arex.inst.extension.ModuleDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ModuleVersionMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    public static ElementMatcher.Junction<ClassLoader> versionMatch(ModuleDescription description) {
        return new IgnoreClassloaderMatcher(new ModuleVersionMatcher(description));
    }

    /**
     * Don't use static final, because each matcher instance has a copy of CACHE
     */
    private final ConcurrentCache<ClassLoader, Boolean> cache = new ConcurrentCache<>(8);
    private final ModuleDescription description;

    ModuleVersionMatcher(ModuleDescription description) {
        this.description = description;
    }

    @Override
    public boolean matches(ClassLoader cl) {
        if (cl == null) {
            return false;
        }
        return description == null || cache.computeIfAbsent(cl, this::versionMatches);
    }

    private boolean versionMatches(ClassLoader loader) {
        ResourceManager.registerResources(loader);
        if (!LoadedModuleCache.exist(description.getModuleName())) {
            return false;
        }
        Pair<Integer, Integer> version = LoadedModuleCache.get(description.getModuleName());
        if (version == null) {
            // to avoid duplicate transform of the same class in different module
            return false;
        }
        return description.isSupported(version);
    }
}
