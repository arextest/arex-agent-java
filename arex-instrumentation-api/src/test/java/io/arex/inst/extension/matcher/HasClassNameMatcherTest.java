package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.Test;

class HasClassNameMatcherTest {

    @Test
    void hasClassNamed() {
        ElementMatcher.Junction<ClassLoader> matcher = HasClassNameMatcher.hasClassNamed("io.arex.inst.extension.matcher.HasClassNameMatcher");
        assertNotNull(matcher);
    }

    @Test
    void matches() {
        ElementMatcher.Junction<ClassLoader> matcher = new HasClassNameMatcher("io.arex.inst.extension.matcher.HasClassNameMatcher");
        assertFalse(matcher.matches(null));
        assertTrue(matcher.matches(Thread.currentThread().getContextClassLoader()));
    }
}