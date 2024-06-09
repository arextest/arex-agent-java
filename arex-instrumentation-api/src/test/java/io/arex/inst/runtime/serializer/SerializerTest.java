package io.arex.inst.runtime.serializer;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.listener.EventProcessorTest.TestJacksonSerializable;
import io.arex.inst.runtime.listener.EventProcessorTest.TestGsonSerializer;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.TypeUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class SerializerTest {
    private static StringSerializable jacksonSerializerWithType;
    @BeforeAll
    static void setUp() {
        final List<StringSerializable> list = new ArrayList<>(2);
        jacksonSerializerWithType = Mockito.mock(StringSerializable.class);
        Mockito.when(jacksonSerializerWithType.name()).thenReturn(ArexConstants.JACKSON_SERIALIZER_WITH_TYPE);
        Mockito.when(jacksonSerializerWithType.isDefault()).thenReturn(false);
        list.add(new TestJacksonSerializable());
        list.add(new TestGsonSerializer());
        list.add(jacksonSerializerWithType);
        Serializer.builder(list).build();
    }

    @AfterAll
    static void tearDown() {
        jacksonSerializerWithType = null;
        Mockito.clearAllCaches();
    }

    @Test
    void builder() {
        assertNotNull(Serializer.getINSTANCE());
        assertEquals(3, Serializer.getINSTANCE().getSerializers().size());
    }

    @Test
    void testThrowError() {
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
        String nullListJson = Serializer.serialize(list, "jackson");
        assertEquals("[null]", nullListJson);
        String nullListTypeName = TypeUtil.getName(list);
        Object deserialize = Serializer.deserialize(nullListJson, nullListTypeName);
        assertEquals(list, deserialize);

        list.add(new ArrayList<>());
        list.add(nestedList1);
        list.add(nestedList2);

        String json = Serializer.serialize(list, "jackson");
        String typeName = TypeUtil.getName(list);
        System.out.println(typeName);
        System.out.println(json);

        List<List<Object>> actualResult = Serializer.deserialize(json, typeName);
        assertEquals(list, actualResult);

        // empty list
        String emptyListTypeName = TypeUtil.getName(new ArrayList<>());
        List<Object> emptyList = Serializer.deserialize("[]", emptyListTypeName);
        assertEquals(0, emptyList.size());
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

    @Test
    void testInitSerializerConfigMap() throws Exception {
        Method initSerializerConfigMap = Serializer.class.getDeclaredMethod("initSerializerConfigMap");
        initSerializerConfigMap.setAccessible(true);
        // null config
        final Field instance = Config.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
        initSerializerConfigMap.invoke(null);
        Assertions.assertDoesNotThrow(() -> Serializer.getSerializerFromType("dubboRequest"));

        // empty serializer config
        ConfigBuilder builder = new ConfigBuilder("testSerializer");
        builder.build();
        initSerializerConfigMap.invoke(null);
        assertNull(Serializer.getSerializerFromType("dubboRequest"));

        // serializer config
        builder = new ConfigBuilder("testSerializer");
        builder.addProperty(ConfigConstants.SERIALIZER_CONFIG, "soa:gson,dubboRequest:jackson,httpRequest");
        builder.build();
        initSerializerConfigMap.invoke(null);
        assertEquals("jackson", Serializer.getSerializerFromType("dubboRequest"));
        assertEquals("gson", Serializer.getSerializerFromType("soa"));
        assertNull(Serializer.getSerializerFromType("httpRequest"));
    }

    @Test
    void testTypeIsException() {
        final RuntimeException runtimeException = new RuntimeException();
        final String json = Serializer.serialize(runtimeException);
        String typeName = TypeUtil.getName(runtimeException);
        assertNotNull(json);
        final RuntimeException actualResult = Serializer.deserialize(json, TypeUtil.forName(typeName));
        assertEquals(runtimeException.getClass(), actualResult.getClass());
    }

    @Test
    void serializeWithType() throws Throwable {
        // null object
        assertNull(Serializer.serializeWithType(null));

        // normal object
        final String json = Serializer.serializeWithType("test");
        Mockito.verify(jacksonSerializerWithType, Mockito.times(1)).serialize("test");

        // throw exception
        Mockito.when(jacksonSerializerWithType.serialize("test")).thenThrow(new RuntimeException());
        assertNull(Serializer.serializeWithType("test"));
        assertDoesNotThrow(() -> Serializer.serializeWithType("test"));
    }

    @Test
    void deserializeWithType() throws Throwable {
        // null json
//        assertNull(Serializer.deserializeWithType(null));
//
//        // throw exception
//        Mockito.when(jacksonSerializerWithType.deserialize("test", Object.class)).thenThrow(new RuntimeException());
//        assertDoesNotThrow(() -> Serializer.deserializeWithType("test"));
        String groupName = "[\"agg-hotel-common\"]";
        List<String> list = Serializer.deserialize(groupName, List.class);
        System.out.println(list.contains("agg-hotel-common"));
    }
}
