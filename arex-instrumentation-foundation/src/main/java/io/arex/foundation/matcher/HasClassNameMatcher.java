package io.arex.foundation.matcher;

import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.WeakHashMap;

public class HasClassNameMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {
    public static ElementMatcher.Junction<ClassLoader> hasClassNamed(String className) {
        return new HasClassNameMatcher(className);
    }

    private final Map<ClassLoader, Boolean> cache = new WeakHashMap<>();
    private final String className;


    HasClassNameMatcher(String className) {
        this.className = className.replace(".", "/") + ".class";
    }

    @Override
    public boolean matches(ClassLoader cl) {
        if (cl == null) {
            return false;
        }
        return cache.computeIfAbsent(cl, this::hasResources);
    }

    private boolean hasResources(ClassLoader cl) {
        return cl.getResource(className) != null;
    }
}
