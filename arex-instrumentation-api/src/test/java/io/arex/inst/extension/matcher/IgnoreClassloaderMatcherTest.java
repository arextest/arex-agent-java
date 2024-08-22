package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URLClassLoader;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class IgnoreClassloaderMatcherTest {

    @Test
    void testMatchesMatcher() {
        IgnoreClassloaderMatcher matcher = new IgnoreClassloaderMatcher(
            new HasClassNameMatcher("io.arex.inst.extension.matcher.HasClassNameMatcher"));

        assertFalse(matcher.matches(null));

        assertTrue(matcher.matches(Thread.currentThread().getContextClassLoader()));
    }

    @Test
    void testMatchesIgnoredLoaders() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        IgnoreClassloaderMatcher matcher = new IgnoreClassloaderMatcher(Collections.singletonList(contextClassLoader.getClass().getName()));

        assertFalse(matcher.matches(null));

        // url class loader not matches ignored class loaders
        assertFalse(matcher.matches(URLClassLoader.class.getClassLoader()));

        // app class loader, matches ignored class loaders
        assertTrue(matcher.matches(contextClassLoader), () -> "contextClassLoader: " + contextClassLoader.getClass().getName());

        // matcher is null
        assertFalse(matcher.matches(contextClassLoader.getParent()));
    }
}
