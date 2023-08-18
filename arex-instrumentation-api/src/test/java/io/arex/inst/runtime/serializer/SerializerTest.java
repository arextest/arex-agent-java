package io.arex.inst.runtime.serializer;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.listener.EventProcessorTest.TestJacksonSerializable;
import io.arex.inst.runtime.listener.EventProcessorTest.TestGsonSerializer;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SerializerTest {
    @BeforeAll
    static void setUp() {
        final List<StringSerializable> list = new ArrayList<>(2);
        list.add(new TestJacksonSerializable());
        list.add(new TestGsonSerializer());
        Serializer.builder(list).build();
    }

    @AfterAll
    static void tearDown() {

    }

    @Test
    void builder() {
        assertNotNull(Serializer.getINSTANCE());
        assertEquals(1, Serializer.getINSTANCE().getSerializers().size());
    }

    @Test
    void testThrowError() {
        final List<StringSerializable> list = new ArrayList<>();
        list.add(new TestJacksonSerializable());
        list.add(new TestGsonSerializer());
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
    void testNestedList() {
        List<List<Object>> list = new ArrayList<>();
        List<Object> nestedList1 = new ArrayList<>();
        nestedList1.add("nestList1-1");
        nestedList1.add("nestList1-2");

        List<Object> nestedList2 = new LinkedList<>();
        nestedList2.add("nestList2-1");
        nestedList2.add("nestList2-2");

        list.add(null);
        list.add(new ArrayList<>());
        list.add(nestedList1);
        list.add(nestedList2);

        String json = Serializer.serialize(list, "jackson");
        String typeName = TypeUtil.getName(list);
        System.out.println(typeName);
        System.out.println(json);

        List<List<Object>> actualResult = Serializer.deserialize(json, typeName);
        assertEquals(list, actualResult);
    }

    @Test
    void testNestedSet() {
        Set<Set<Object>> set = new HashSet<>();
        Set<Object> nestedSet1 = new HashSet<>();
        nestedSet1.add("nestedSet1-1");
        nestedSet1.add("nestedSet1-2");

        Set<Object> nestedSet2 = new TreeSet<>();
        nestedSet2.add("nestedSet2-1");
        nestedSet2.add("nestedSet2-2");

        set.add(null);
        set.add(new HashSet<>());
        set.add(nestedSet1);
        set.add(nestedSet2);

        String json = Serializer.serialize(set, "jackson");
        String typeName = TypeUtil.getName(set);
        System.out.println(typeName);
        System.out.println(json);

        Set<Set<Object>> actualResult = Serializer.deserialize(json, typeName);
        assertEquals(set, actualResult);
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