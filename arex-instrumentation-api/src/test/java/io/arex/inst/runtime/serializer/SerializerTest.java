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

    @Test
    void testSerializeThrowException() throws Throwable {
        final List<StringSerializable> list = new ArrayList<>();
        list.add(new TestStringSerializable());
        list.add(new TestStringSerializer2());
        Serializer.builder(list).build();

        // null
        assertNull(Serializer.serializeWithException(null, null));

        // serialize throw error
        Assertions.assertThrows(Throwable.class, () -> Serializer.serializeWithException("test", null));

        // serialize normal
        List<List<String>> list2 = new ArrayList<>();
        final List<String> innerList = new ArrayList<>();
        final List<String> innerList2 = new ArrayList<>();
        innerList.add("test");
        innerList2.add("test2");
        list2.add(innerList);
        list2.add(innerList2);
        Assertions.assertDoesNotThrow(() -> Serializer.serializeWithException(list2, "gson"));
    }

    @Test
    void nullObjectOrType() {
        // null
        assertNull(Serializer.serialize(null));
        assertNull(Serializer.deserialize(null, TypeUtil.forName(null)));
        assertNull(Serializer.deserialize(null, (String)null));
        assertNull(Serializer.deserialize(null, (Class<?>)null));

        // serialize Throwable
        Assertions.assertDoesNotThrow(() -> Serializer.serialize(new Throwable()));
    }
}