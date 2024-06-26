package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.model.ArexConstants;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    private static final String[] IGNORED_STARTS_WITH_NAME = new String[]{
        "io.arex.", "shaded.", IgnoreClassloaderMatcher.BYTE_BUDDY_PREFIX,
        "sun.reflect.", "org.springframework.boot.autoconfigure", "com.intellij.", ArexConstants.OT_TYPE_PREFIX};

    private static final String[] IGNORED_CONTAINS_NAME = new String[]{"javassist.", ".asm.", ".reflectasm."};

    private List<String> customIgnoredTypePrefixes = Collections.emptyList();

    public IgnoredTypesMatcher(List<String> customIgnoredTypePrefixes) {
        if (!CollectionUtil.isEmpty(customIgnoredTypePrefixes)) {
            this.customIgnoredTypePrefixes = customIgnoredTypePrefixes;
        }
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (target.isSynthetic()) {
            return true;
        }
        String name = target.getActualName();

        for (String ignored : IGNORED_STARTS_WITH_NAME) {
            if (name.startsWith(ignored)) {
                return true;
            }
        }

        for (String ignored : IGNORED_CONTAINS_NAME) {
            if (name.contains(ignored)) {
                return true;
            }
        }

        for (String ignored : customIgnoredTypePrefixes) {
            if (name.startsWith(ignored)) {
                return true;
            }
        }

        // check if the target is in the AdviceInjectorCache
        return AdviceInjectorCache.contains(target.getName());
    }
}
