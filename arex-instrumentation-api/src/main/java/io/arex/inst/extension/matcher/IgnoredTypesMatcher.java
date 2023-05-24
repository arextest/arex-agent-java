package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    private static final String[] IGNORED_STARTS_WITH_NAME = new String[]{
        "io.arex.", "shaded.", IgnoreClassloaderMatcher.BYTE_BUDDY_PREFIX,
        "sun.reflect.", "org.springframework.boot.autoconfigure", "com.intellij."};

    private static final String[] IGNORED_CONTAINS_NAME = new String[]{"javassist.", ".asm.", ".reflectasm."};

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

        return false;
    }
}
