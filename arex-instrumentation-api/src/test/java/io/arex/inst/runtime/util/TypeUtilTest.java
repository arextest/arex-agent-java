package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

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

}