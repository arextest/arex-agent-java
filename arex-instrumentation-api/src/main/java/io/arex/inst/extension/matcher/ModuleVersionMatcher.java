package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.ConcurrentCache;
import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.inst.runtime.context.ResourceManager;
import io.arex.inst.extension.ModuleDescription;
import java.util.Collection;
import java.util.Set;
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
        String version;
        for (String moduleName : description.getModuleNames()) {
            version = LoadedModuleCache.get(moduleName);
            if (version != null) {
                return description.isSupported(ComparableVersion.of(version));
            }
        }
        // to avoid duplicate transform of the same class in different module
        return false;
    }
}
