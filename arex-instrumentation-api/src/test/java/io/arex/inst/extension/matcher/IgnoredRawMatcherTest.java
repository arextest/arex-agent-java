package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.Test;

import java.security.ProtectionDomain;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IgnoredRawMatcherTest {

    @Test
    void matches() {
        ProtectionDomain protectionDomain = new ProtectionDomain(null, null);
        TypeDescription.ForLoadedType typeDescription = new TypeDescription.ForLoadedType(String.class);
        ClassLoader classLoader = IgnoredRawMatcherTest.class.getClassLoader();

        // case 1
        // ignore type: false (classloader is null)
        IgnoredRawMatcher matcher1 = new IgnoredRawMatcher(Collections.emptyList(), Collections.emptyList());
        assertDoesNotThrow(() -> matcher1.matches(typeDescription, null, null, null, protectionDomain));

        // case 2
        // ignore type: false
        // ignore classloader: false
        IgnoredRawMatcher matcher2 = new IgnoredRawMatcher(Collections.emptyList(), Collections.emptyList());
        assertDoesNotThrow(() -> matcher2.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 3
        // ignore type: true
        // ignore classloader: false
        IgnoredRawMatcher matcher3 = new IgnoredRawMatcher(Collections.singletonList(typeDescription.getActualName()), Collections.emptyList());
        assertDoesNotThrow(() -> matcher3.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 4
        // ignore type: false
        // ignore classloader: true
        IgnoredRawMatcher matcher4 = new IgnoredRawMatcher(Collections.emptyList(), Collections.singletonList(classLoader.getClass().getName()));
        assertDoesNotThrow(() -> matcher4.matches(typeDescription, classLoader, null, null, protectionDomain));

        // case 3
        // ignore type: true
        // ignore classloader: true
        assertDoesNotThrow(() -> matcher4.matches(typeDescription, classLoader, null, null, protectionDomain));
    }
}
