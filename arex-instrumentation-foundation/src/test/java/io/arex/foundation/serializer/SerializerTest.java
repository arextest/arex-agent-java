//package io.arex.foundation.serializer;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import io.arex.agent.bootstrap.util.CollectionUtil;
//import io.arex.inst.runtime.serializer.Serializer;
//import io.arex.inst.runtime.util.TypeUtil;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//class SerializerTest {
//
//    @BeforeEach
//    void setUp() {
//        Serializer.builder(JacksonSerializer.INSTANCE).build();
//    }
//
//    @AfterEach
//    void tearDown() {
//
//    }
//
//    @Test
//    void deserializeValueTypeName() {
//        List<List<Object>> doubleList = new ArrayList<>();
//
//        doubleList.add(CollectionUtil.newArrayList("a", "b", "c"));
//        doubleList.add(CollectionUtil.newArrayList(1, 2, 3));
//        String typeName = TypeUtil.getName(doubleList);
//
//        String json = Serializer.serialize(doubleList);
//
//        List<List<Object>> actualResult = Serializer.deserialize(json, typeName);
//
//        assertEquals(doubleList, actualResult);
//    }
//
//    @Test
//    void deserializeHashMapValues() {
//        Map<String, LocalDate> map = new HashMap<>();
//        map.put("key1", LocalDate.now());
//
//        Collection<?> values = map.values();
//
//        String typeName = TypeUtil.getName(values);
//        String json = Serializer.serialize(values);
//        assertEquals("java.util.HashMap$Values-java.time.LocalDate", typeName);
//
//        Collection<?> collection = Serializer.deserialize(json, typeName);
//        String actualResult = TypeUtil.getName(collection);
//        assertEquals("java.util.HashMap$Values-java.time.LocalDate", actualResult);
//        for (Object e : collection) {
//            assertInstanceOf(LocalDate.class, e);
//        }
//    }
//}