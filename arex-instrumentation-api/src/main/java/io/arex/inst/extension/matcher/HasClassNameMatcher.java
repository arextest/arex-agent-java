package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.util.ConcurrentCache;
import net.bytebuddy.matcher.ElementMatcher;

public class HasClassNameMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    public static ElementMatcher.Junction<ClassLoader> hasClassNamed(String className) {
        return new IgnoreClassloaderMatcher(new HasClassNameMatcher(className));
    }

    /**
     * Don't use static final, because each matcher instance has a copy of CACHE
     */
    private final ConcurrentCache<ClassLoader, Boolean> cache = new ConcurrentCache<>(8);
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
