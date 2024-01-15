package io.arex.inst.extension.matcher;

import static org.junit.jupiter.api.Assertions.*;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

/**
 * @since 2024/1/12
 */
class SafeExtendsClassMatcherTest {

    @Test
    void extendsClass() {
        ElementMatcher.Junction<TypeDescription> matcher = SafeExtendsClassMatcher.extendsClass(
            ElementMatchers.is(SafeExtendsClassMatcher.class));

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(SafeExtendsClassMatcher.class)));
    }

    @Test
    void testExtendsClass() {
        ElementMatcher.Junction<TypeDescription> matcher = SafeExtendsClassMatcher.extendsClass(
            ElementMatchers.is(ElementMatcher.Junction.AbstractBase.class), true);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(SafeExtendsClassMatcher.class)));
    }
}
