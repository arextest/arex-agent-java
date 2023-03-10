package io.arex.inst.httpservlet.inst;

import jakarta.servlet.Filter;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FilterInstrumentationV5Test {

    FilterInstrumentationV5 inst = new FilterInstrumentationV5();

    @BeforeAll
    static void setUp() {
    }

    @AfterAll
    static void tearDown() {
    }

    @Test
    void typeMatcher() {
        assertFalse(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(Filter.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void adviceClassNames() {
        assertEquals(11, inst.adviceClassNames().size());
    }
}