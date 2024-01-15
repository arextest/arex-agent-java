package io.arex.inst.runtime.util.sizeof;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;

class ObjectGraphWalkerTest {
    static ObjectGraphWalker caller;
    static SizeOfFilter sizeOfFilter;
    static ObjectGraphWalker.Visitor visitor;

    @BeforeAll
    static void setUp() {
        sizeOfFilter = Mockito.mock(SizeOfFilter.class);
        visitor = Mockito.mock(ObjectGraphWalker.Visitor.class);
        caller = new ObjectGraphWalker(visitor, sizeOfFilter);
    }

    @AfterAll
    static void tearDown() {
        caller = null;
        sizeOfFilter = null;
        visitor = null;
        Mockito.clearAllCaches();
    }

    @Test
    void walk() {
        caller.walk(null);
    }

    @Test
    void testWalk() {
        VisitorListener visitorListener = Mockito.mock(VisitorListener.class);
        Mockito.when(sizeOfFilter.filterClass(any())).thenReturn(true);
        Collection<Field> result = new ArrayList<>();
        result.add(TestWalker.class.getDeclaredFields()[0]);
        Mockito.when(sizeOfFilter.filterFields(any(), any())).thenReturn(result);
        caller.walk(visitorListener, new Object[]{new TestWalker[]{new TestWalker()}});
    }

    static class TestWalker {
        String name;
    }
}