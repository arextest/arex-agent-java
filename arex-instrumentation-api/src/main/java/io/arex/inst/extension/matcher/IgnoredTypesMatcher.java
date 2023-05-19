package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    private static final List<String> IGNORED_TYPES = Arrays.asList(
            "net.bytebuddy.",
            "io.arex.",
            "sun.reflect.",
            "com.intellij.",
            "shaded.");

    @Override
    public boolean matches(TypeDescription target) {
        String name = target.getActualName();
        for (String ignoredType : IGNORED_TYPES) {
            if (name.startsWith(ignoredType)) {
                return true;
            }
        }
        return false;
    }
}
