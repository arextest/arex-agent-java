package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

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
        List<String> ignoredClassLoaders = Arrays.asList("sun.reflect", "jdk.internal.reflect", "net.bytebuddy.");
        IgnoreClassloaderMatcher matcher = new IgnoreClassloaderMatcher(ignoredClassLoaders);

        assertFalse(matcher.matches(null));

        // url class loader not matches ignored class loaders
        assertFalse(matcher.matches(URLClassLoader.class.getClassLoader()));

        // app class loader, not matches ignored class loaders
        assertFalse(matcher.matches(Thread.currentThread().getContextClassLoader()));
    }
}
