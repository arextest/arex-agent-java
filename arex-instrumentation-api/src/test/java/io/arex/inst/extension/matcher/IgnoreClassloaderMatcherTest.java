package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class IgnoreClassloaderMatcherTest {

    @Test
    void matches() {
        IgnoreClassloaderMatcher matcher = new IgnoreClassloaderMatcher(
            new HasClassNameMatcher("io.arex.inst.extension.matcher.HasClassNameMatcher"));

        assertFalse(matcher.matches(null));

        assertTrue(matcher.matches(Thread.currentThread().getContextClassLoader()));
    }
}