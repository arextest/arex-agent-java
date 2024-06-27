package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.Test;

import java.security.ProtectionDomain;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IgnoredRawMatcherTest {

    @Test
    void matches() {
        ProtectionDomain protectionDomain = new ProtectionDomain(null, null);
        TypeDescription.ForLoadedType typeDescription = new TypeDescription.ForLoadedType(String.class);
        ClassLoader classLoader = IgnoredRawMatcherTest.class.getClassLoader();

        // case 1
        // ignore type: false (classloader is null)
        IgnoredRawMatcher matcher = new IgnoredRawMatcher(Collections.emptyList(), Collections.emptyList());
        assertFalse(matcher.matches(typeDescription, null, null, null, protectionDomain));

        // case 2
        // ignore type: false
        // ignore classloader: false
        matcher = new IgnoredRawMatcher(Collections.emptyList(), Collections.emptyList());
        assertFalse(matcher.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 3
        // ignore type: true
        // ignore classloader: false
        matcher = new IgnoredRawMatcher(Collections.singletonList(typeDescription.getActualName()), Collections.emptyList());
        assertTrue(matcher.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 4
        // ignore type: false
        // ignore classloader: true
        matcher = new IgnoredRawMatcher(Collections.emptyList(), Collections.singletonList(classLoader.getClass().getName()));
        assertTrue(matcher.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 3
        // ignore type: true
        // ignore classloader: true
        matcher = new IgnoredRawMatcher(Collections.singletonList(typeDescription.getActualName()), Collections.singletonList(classLoader.getClass().getName()));
        assertTrue(matcher.matches(typeDescription, classLoader, null, null, protectionDomain));
    }
}