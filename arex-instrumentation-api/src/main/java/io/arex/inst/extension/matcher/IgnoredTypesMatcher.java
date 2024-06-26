package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.util.CollectionUtil;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    private static final List<String> IGNORED_TYPE_PREFIXES = CollectionUtil.newArrayList(
        "io.arex.", "shaded.", IgnoreClassloaderMatcher.BYTE_BUDDY_PREFIX,
        "sun.reflect.", "org.springframework.boot.autoconfigure", "com.intellij.");

    private static final String[] IGNORED_CONTAINS_NAME = new String[]{"javassist.", ".asm.", ".reflectasm.", IgnoreClassloaderMatcher.BYTE_BUDDY_PREFIX};

    public IgnoredTypesMatcher(List<String> ignoreTypePrefixes) {
        if (ignoreTypePrefixes != null) {
            IGNORED_TYPE_PREFIXES.addAll(ignoreTypePrefixes);
        }
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (target.isSynthetic()) {
            return true;
        }
        String name = target.getActualName();

        for (String ignored : IGNORED_TYPE_PREFIXES) {
            if (name.startsWith(ignored)) {
                return true;
            }
        }

        for (String ignored : IGNORED_CONTAINS_NAME) {
            if (name.contains(ignored)) {
                return true;
            }
        }

        // check if the target is in the AdviceInjectorCache
        return AdviceInjectorCache.contains(target.getName());
    }
}
