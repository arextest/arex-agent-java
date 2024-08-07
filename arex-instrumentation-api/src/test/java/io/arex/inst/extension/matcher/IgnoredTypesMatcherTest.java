package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class IgnoredTypesMatcherTest {

    @Test
    void matches() {
        IgnoredTypesMatcher matcher = new IgnoredTypesMatcher(Collections.emptyList());
        assertDoesNotThrow(() -> matcher.matches(new TypeDescription.ForLoadedType(IgnoredTypesMatcher.class)));
        assertDoesNotThrow(() -> matcher.matches(new TypeDescription.ForLoadedType(String.class)));
    }
}