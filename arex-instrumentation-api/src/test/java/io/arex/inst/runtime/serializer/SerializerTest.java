package io.arex.inst.runtime.serializer;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.listener.EventProcessorTest;
import io.arex.inst.runtime.listener.EventProcessorTest.TestStringSerializable;
import io.arex.inst.runtime.listener.EventProcessorTest.TestStringSerializer2;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SerializerTest {

    @Test
    void builder() {
        Serializer.builder(new EventProcessorTest.TestStringSerializable()).build();
        assertNotNull(Serializer.getINSTANCE());
        assertEquals(0, Serializer.getINSTANCE().getSerializers().size());
    }

    @Test
    void testBuilder() {
        final List<StringSerializable> list = new ArrayList<>();
        list.add(new TestStringSerializable());
        list.add(new TestStringSerializer2());
        Serializer.builder(list).build();
        assertNotNull(Serializer.getINSTANCE());
        assertEquals(1, Serializer.getINSTANCE().getSerializers().size());
    }

    @Test
    void testThrowError() {
        final List<StringSerializable> list = new ArrayList<>();
        list.add(new TestStringSerializable());
        list.add(new TestStringSerializer2());
        Serializer.builder(list).build();
        // serialize throw error
        Assertions.assertDoesNotThrow(() -> Serializer.serialize("test"));

        // deserialize throw error
        Assertions.assertDoesNotThrow(() -> Serializer.deserialize("test", String.class));

        // deserialize throw error type
        Assertions.assertDoesNotThrow(() -> Serializer.deserialize("test", TypeUtil.forName("java.lang.String")));

        // deserialize throw error type name
        Assertions.assertDoesNotThrow(() -> Serializer.deserialize("test", "java.lang.String"));
    }
}