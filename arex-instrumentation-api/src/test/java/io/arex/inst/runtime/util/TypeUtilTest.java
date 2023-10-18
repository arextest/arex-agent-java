package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.model.ParameterizedTypeImpl;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TypeUtilTest {

    @ParameterizedTest
    @MethodSource("getNameArguments")
    void testGetName(Object result, Predicate<String> predicate) {
        String actualResult = TypeUtil.getName(result);
        assertTrue(predicate.test(actualResult));
    }

    public static Stream<Arguments> getNameArguments() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        List<String> innerList = CollectionUtil.newArrayList(null, "test");
        List<LocalDateTime> innerList2 = CollectionUtil.newArrayList(LocalDateTime.now());
        List<List<?>> nestedList = CollectionUtil.newArrayList(null, innerList, innerList2);

        return Stream.of(
            arguments(null, (Predicate<String>) Objects::isNull),
            arguments(new HashMap<>(), (Predicate<String>) "java.util.HashMap"::equals),
            arguments(map, (Predicate<String>) "java.util.HashMap-java.lang.String,java.lang.String"::equals),
            arguments(Optional.of("test-optional"), (Predicate<String>) "java.util.Optional-java.lang.String"::equals),
            arguments(CollectionUtil.emptyList(), (Predicate<String>) "java.util.ArrayList"::equals),
            arguments(innerList2, (Predicate<String>) "java.util.ArrayList-java.time.LocalDateTime"::equals),
            arguments(nestedList,
                (Predicate<String>) "java.util.ArrayList-java.util.ArrayList,java.lang.String,java.time.LocalDateTime"::equals),
            arguments(ParameterizedTypeImpl.make(ArrayList.class, new Type[]{String.class}, null),
                (Predicate<String>) "java.util.ArrayList-java.lang.String"::equals),
            arguments(TypeUtilTest.class, (Predicate<String>) "io.arex.inst.runtime.util.TypeUtilTest"::equals)
        );
    }

    @ParameterizedTest
    @MethodSource("forNameArguments")
    void testForName(String typeName, Predicate<Type> predicate) {
        Type actualResult = TypeUtil.forName(typeName);
        assertTrue(predicate.test(actualResult));
    }

    public static Stream<Arguments> forNameArguments() {
        return Stream.of(
            arguments(null, (Predicate<Type>) Objects::isNull),
            arguments("-", (Predicate<Type>) Objects::isNull),
            arguments(" - ", (Predicate<Type>) Objects::isNull),
            arguments("java.util.ArrayList-", (Predicate<Type>) type -> "java.util.ArrayList".equals(type.getTypeName())),
            arguments("java.util.ArrayList-java.util.HashMap-java.lang.String,java.lang.String", (Predicate<Type>) type -> {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                return "java.util.ArrayList".equals(parameterizedType.getRawType().getTypeName())
                    && "java.util.HashMap".equals(((ParameterizedType)parameterizedType.getActualTypeArguments()[0]).getRawType().getTypeName());
            }),
            arguments("java.util.ArrayList-java.lang.String", (Predicate<Type>) type -> {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                return "java.util.ArrayList".equals(parameterizedType.getRawType().getTypeName())
                    && "java.lang.String".equals(parameterizedType.getActualTypeArguments()[0].getTypeName());
            }),
            arguments("java.util.HashMap$Values-java.lang.String", (Predicate<Type>) type -> {
                final Class<?> rawClass = TypeUtil.getRawClass(type);
                return "java.util.HashMap$Values".equals(rawClass.getName());
            })
        );
    }


    @Test
    void getRawClass() {
        Class<?> actualResult = TypeUtil.getRawClass(String.class);
        assertEquals("java.lang.String", actualResult.getName());

        actualResult = TypeUtil.getRawClass(ParameterizedTypeImpl.make(ArrayList.class, new Type[]{String.class}, null));
        assertEquals("java.util.ArrayList", actualResult.getName());
    }

    @Test
    void testDoubleMap() {
        Map<String, Map<String, LocalDateTime>> map = new HashMap<>();
        Map<String, LocalDateTime> innerMap = new HashMap<>();
        innerMap.put("key1", LocalDateTime.now());
        map.put("key", innerMap);
        String actualResult = TypeUtil.getName(map);
        assertEquals("java.util.HashMap-java.lang.String,java.util.HashMap-java.lang.String,java.time.LocalDateTime", actualResult);
        final Type type = TypeUtil.forName(actualResult);
        assert type != null;
        assertEquals("java.util.HashMap<java.lang.String, java.util.HashMap<java.lang.String, java.time.LocalDateTime>>", type.getTypeName());
    }
    @Test
    void testListMap() {
        List<Map<String, LocalDateTime>> list = new ArrayList<>();
        Map<String, LocalDateTime> innerMap = new HashMap<>();
        innerMap.put("key1", LocalDateTime.now());
        list.add(innerMap);
        String actualResult = TypeUtil.getName(list);
        assertEquals("java.util.ArrayList-java.util.HashMap-java.lang.String,java.time.LocalDateTime", actualResult);
        final Type type = TypeUtil.forName(actualResult);
        assert type != null;
        assertEquals("java.util.ArrayList<java.util.HashMap<java.lang.String, java.time.LocalDateTime>>", type.getTypeName());
    }

    @Test
    void testMapList() {
        Map<String, List<LocalDateTime>> map = new HashMap<>();
        List<LocalDateTime> innerList = new ArrayList<>();
        innerList.add(LocalDateTime.now());
        map.put("key", innerList);
        String actualResult = TypeUtil.getName(map);
        assertEquals("java.util.HashMap-java.lang.String,java.util.ArrayList-java.time.LocalDateTime", actualResult);
        final Type type = TypeUtil.forName(actualResult);
        assert type != null;
        assertEquals("java.util.HashMap<java.lang.String, java.util.ArrayList<java.time.LocalDateTime>>", type.getTypeName());
    }

    @Test
    void testNoGeneric() {
        LocalDateTime localDateTime = LocalDateTime.now();
        final String name = TypeUtil.getName(localDateTime);
        assertEquals("java.time.LocalDateTime", name);
        final Type type = TypeUtil.forName(name);
        assert type != null;
        assertEquals("java.time.LocalDateTime", type.getTypeName());
    }

    @Test
    void testDoubleGenericType() {
        final Pair pair = Pair.of(LocalDateTime.now(), LocalDate.now());
        final String name = TypeUtil.getName(pair);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.time.LocalDateTime,java.time.LocalDate", name);
        final Type type = TypeUtil.forName(name);
        assert type != null;
        assertEquals("io.arex.agent.bootstrap.internal.Pair<java.time.LocalDateTime, java.time.LocalDate>", type.getTypeName());

        final Pair pair2 = Pair.of(System.currentTimeMillis(), LocalTime.now());
        final String name2 = TypeUtil.getName(pair2);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.lang.Long,java.time.LocalTime", name2);
        final Type type2 = TypeUtil.forName(name2);
        assert type2 != null;
        assertEquals("io.arex.agent.bootstrap.internal.Pair<java.lang.Long, java.time.LocalTime>", type2.getTypeName());

        final Pair pairNull = Pair.of(System.currentTimeMillis(), null);
        final String genericNull = TypeUtil.getName(pairNull);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.lang.Long,", genericNull);

        final Pair pairList = Pair.of(System.currentTimeMillis(), Arrays.asList("mock"));
        final String genericList = TypeUtil.getName(pairList);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.lang.Long,java.util.Arrays$ArrayList-java.lang.String", genericList);
    }

    @Test
    void testSingle() {
        final Single<LocalTime> localTimeSingle = new Single<>(LocalTime.now());
        final String name = TypeUtil.getName(localTimeSingle);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$Single-java.time.LocalTime", name);
        final Type type = TypeUtil.forName(name);
        assert type != null;
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$Single<java.time.LocalTime>", type.getTypeName());
        final Single<LocalDateTime> localDateTimeSingle = new Single<>(LocalDateTime.now());
        final String name2 = TypeUtil.getName(localDateTimeSingle);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$Single-java.time.LocalDateTime", name2);
        final Type type2 = TypeUtil.forName(name2);
        assert type2 != null;
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$Single<java.time.LocalDateTime>", type2.getTypeName());
    }

    @Test
    void testForNameException() {
        try (MockedStatic<StringUtil> mockedStatic = Mockito.mockStatic(StringUtil.class)) {
            Assertions.assertDoesNotThrow(() -> TypeUtil.forName("java.lang.String"));
        }
    }

    @Test
    void testInvokeGetFieldType() {

        try {
            final Method invokeGetFieldType = TypeUtil.class.getDeclaredMethod("invokeGetFieldType",
                    Field.class, Object.class);
            invokeGetFieldType.setAccessible(true);
            assertNull(invokeGetFieldType.invoke(null, null, null));
            final Field first = Pair.class.getDeclaredField("first");
            assertDoesNotThrow(() -> invokeGetFieldType.invoke(null, first, new Single<>(null)));
            assertNull(invokeGetFieldType.invoke(null, first, new Single<>(null)));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static class Single<T> {
        private final T value;

        public Single(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

    }

    @Test
    public void testNullList() {
        final List<Object> list = new ArrayList<>();
        list.add(null);
        String expectedName = "java.util.ArrayList-";
        final String actualName = TypeUtil.getName(list);
        assertEquals(expectedName, actualName);
    }

    @Test
    void testSerializeObjectToString() {
        // null object
        assertNull(TypeUtil.errorSerializeToString(null));
        // args
        Object[] args = new Object[3];
        String arg1 = "arg1";
        Double arg2 = 2.0;
        LocalDateTime arg3 = LocalDateTime.now();
        args[0] = arg1;
        args[1] = arg2;
        args[2] = arg3;
        String argsType = TypeUtil.errorSerializeToString(args);
        assertEquals("java.lang.String,java.lang.Double,java.time.LocalDateTime", argsType);
        // just one class
        final String arg2Type = TypeUtil.errorSerializeToString(arg2);
        assertEquals("java.lang.Double", arg2Type);
    }

    @Test
    void testSetNestedSet() {
        Set<Object> nestedSet1 = new HashSet<>();
        nestedSet1.add("mark");

        Set<Object> nestedSet2 = new HashSet<>();
        nestedSet2.add(33);

        Set<Set<Object>> set = new HashSet<>();
        set.add(null);
        set.add(new HashSet<>());
        set.add(nestedSet1);
        set.add(nestedSet2);

        String typeName = TypeUtil.getName(set);
        System.out.println(typeName);
    }

    @Test
    void testListNestedList() {
        List<Object> nestedList1 = new ArrayList<>();
        nestedList1.add("mark");

        List<Object> nestedList2 = new ArrayList<>();
        nestedList2.add(33);

        List<List<Object>> list = new ArrayList<>();
        list.add(null);
        list.add(new ArrayList<>());
        list.add(nestedList1);
        list.add(nestedList2);

        String typeName = TypeUtil.getName(list);
        System.out.println(typeName);
        assertEquals("java.util.ArrayList-java.util.ArrayList,java.lang.String,java.lang.Integer", typeName);
    }

    @Test
    void testIsCollection() {
        assertFalse(TypeUtil.isCollection(null));
        assertTrue(TypeUtil.isCollection("java.util.ArrayList"));
        assertTrue(TypeUtil.isCollection("java.util.LinkedList"));
        assertTrue(TypeUtil.isCollection("java.util.LinkedHashSet"));
        assertTrue(TypeUtil.isCollection("java.util.TreeSet"));
        assertTrue(TypeUtil.isCollection("java.util.HashSet"));
        assertTrue(TypeUtil.isCollection("java.util.Collections$EmptyList"));
        assertTrue(TypeUtil.isCollection("java.util.Collections$EmptySet"));
        assertTrue(TypeUtil.isCollection("java.util.ArrayDeque"));
        assertFalse(TypeUtil.isCollection("java.util.Collections$EmptyMap"));
        System.out.println(TypeUtil.getName(Collections.emptyList()));
    }

    @Test
    void testToNestedCollection() {
        Collection<?> actualResult = TypeUtil.toNestedCollection(null);
        assertNull(actualResult);

        actualResult = TypeUtil.toNestedCollection(new HashMap<>());
        assertNull(actualResult);

        Collection<Object> collection = new ArrayList<>();
        actualResult = TypeUtil.toNestedCollection(collection);
        assertNull(actualResult);

        collection.add(null);
        actualResult = TypeUtil.toNestedCollection(collection);
        assertEquals(collection, actualResult);

        collection.add(new ArrayList<>());
        actualResult = TypeUtil.toNestedCollection(collection);
        assertEquals(collection, actualResult);
    }


    @Test
    void testMapToString() {
        // single generic map
        final Map<Integer, String> map = new SingleTypeMap<>();

        // empty map
        final String name1 = TypeUtil.getName(map);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$SingleTypeMap", name1);

        map.put(1, "test");
        final String name = TypeUtil.getName(map);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$SingleTypeMap-java.lang.String", name);
        final Type type = TypeUtil.forName(name);
        assert type != null;
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$SingleTypeMap<java.lang.String>", type.getTypeName());

        // no generic map
        final Map<Integer, String> map2 = new Integer2String();
        map2.put(1, "test");
        final String name2 = TypeUtil.getName(map2);
        assertEquals(Integer2String.class.getName(), name2);
        final Type type2 = TypeUtil.forName(name2);
        assert type2 != null;
        assertEquals(Integer2String.class.getName(), type2.getTypeName());
    }

    @Test
    void testGenericFieldInFather() {
        final ChildClass<Object> childClass = new ChildClass<>();
        final ArrayList<Object> list = new ArrayList<>();
        childClass.setValue(list);
        String name = TypeUtil.getName(childClass);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$ChildClass-", name);
        list.add("test");
        childClass.setValue(list);
        name = TypeUtil.getName(childClass);
        assertEquals("io.arex.inst.runtime.util.TypeUtilTest$ChildClass-java.lang.String", name);
    }


    static class SingleTypeMap<V> extends HashMap<Integer, V> {
    }

    static class Integer2String extends HashMap<Integer, String> {
    }

    static class ChildClass<T> extends ParentClass<T> {
        private String childValue;
        public ChildClass() {

        }
    }

    static class ParentClass<T> {
        private List<T> value;
        public ParentClass() {

        }

        public void setValue(List<T> value) {
            this.value = value;
        }
    }
}
