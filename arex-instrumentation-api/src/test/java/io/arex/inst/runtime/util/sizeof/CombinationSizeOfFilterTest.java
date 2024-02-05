package io.arex.inst.runtime.util.sizeof;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class CombinationSizeOfFilterTest {

    static CombinationSizeOfFilter caller;
    static SizeOfFilter filter;

    @BeforeAll
    static void setUp() {
        filter = Mockito.mock(SizeOfFilter.class);
        caller = new CombinationSizeOfFilter(filter);
    }

    @AfterAll
    static void tearDown() {
        caller = null;
        filter = null;
        Mockito.clearAllCaches();
    }

    @Test
    void filterFields() {
        assertNotNull(caller.filterFields(null, null));
    }

    @Test
    void filterClass() {
        assertFalse(caller.filterClass(null));
        Mockito.when(filter.filterClass(any())).thenReturn(true);
        assertTrue(caller.filterClass(null));
    }
}