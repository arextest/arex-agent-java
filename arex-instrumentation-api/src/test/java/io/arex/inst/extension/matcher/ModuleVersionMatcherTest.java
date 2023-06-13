package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.extension.ModuleDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.Test;

class ModuleVersionMatcherTest {

    @Test
    void versionMatch() {
        ElementMatcher.Junction<ClassLoader> matcher = ModuleVersionMatcher.versionMatch(
            ModuleDescription.builder().build());
        assertNotNull(matcher);
    }

    @Test
    void matches() {
        ElementMatcher.Junction<ClassLoader> matcher = new ModuleVersionMatcher(
            ModuleDescription.builder().name("Arex Agent").supportFrom(0,1).build());
        assertFalse(matcher.matches(null));

        assertFalse(matcher.matches(Thread.currentThread().getContextClassLoader()));

        matcher = new ModuleVersionMatcher(
            ModuleDescription.builder().name("Byte Buddy agent").supportFrom(1,8).build());
        assertTrue(matcher.matches(Thread.currentThread().getContextClassLoader()));
    }
}