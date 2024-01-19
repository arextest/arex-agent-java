package io.arex.inst.runtime.util.sizeof;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThrowableFilterTest {

    @Test
    void filterFields() {
        assertNull(ThrowableFilter.INSTANCE.filterFields(String.class, null));
    }

    @Test
    void filterClass() {
        assertFalse(ThrowableFilter.INSTANCE.filterClass(Throwable.class));
    }
}