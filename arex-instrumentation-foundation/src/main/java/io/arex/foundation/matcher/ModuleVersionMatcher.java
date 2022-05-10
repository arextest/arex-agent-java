package io.arex.foundation.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.api.ModuleDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ModuleVersionMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    public static ElementMatcher.Junction<TypeDescription> moduleMatch(ModuleDescription moduleDescription,
                                                                       ElementMatcher<TypeDescription> matcher) {
        return new ModuleVersionMatcher(moduleDescription, matcher);
    }

    private final ModuleDescription moduleDescription;
    private final ElementMatcher<TypeDescription> matcher;

    public ModuleVersionMatcher(ModuleDescription moduleDescription, ElementMatcher<TypeDescription> matcher) {
        this.moduleDescription = moduleDescription;
        this.matcher = matcher;
    }

    @Override
    public boolean matches(TypeDescription target) {
        // Do not move to constructor method
        if (moduleDescription != null && !LoadedModuleCache.hasResource(moduleDescription.getPackages())) {
            return false;
        }
        return matcher.matches(target);
    }
}
