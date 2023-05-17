package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.internal.Cache;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    private final Cache ignoredTypesCache = Cache.trieCache();

    @Override
    public boolean matches(TypeDescription target) {
        String name = target.getActualName();
        return nameStartsWith(name, "net.bytebuddy.") || nameStartsWith(name, "io.arex.")
                || nameStartsWith(name, "sun.reflect.") || nameStartsWith(name, "com.intellij.")
                || nameStartsWith(name, "shaded.");
    }

    private static boolean nameStartsWith(String value, String prefix) {
        /*if (needLog(value)) {
            System.out.println("[AREX][debug] value: " + value + ". prefix: " + prefix);
        }*/
        return value.startsWith(prefix);
    }

    private static boolean needLog(String value) {
        return value.contains("bytebuddy") || value.contains("arex") ||
                value.contains("reflect") || value.contains("intellij") || value.contains("shaded");
    }
}
