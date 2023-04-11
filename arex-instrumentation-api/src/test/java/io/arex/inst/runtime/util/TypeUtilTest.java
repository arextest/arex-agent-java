package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    void getListMapName() {
        String actualResult = TypeUtil.getListMapName(null);
        assertNull(actualResult);

        actualResult = TypeUtil.getListMapName(new ArrayList<>());
        assertEquals("java.util.ArrayList", actualResult);

        List<Map<?, ?>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        list.add(map);
        actualResult = TypeUtil.getListMapName(list);
        assertEquals("java.util.ArrayList-java.util.HashMap", actualResult);


        map.put("key1", "value1");
        actualResult = TypeUtil.getListMapName(list);
        assertEquals("java.util.ArrayList-java.util.HashMap-java.lang.String,java.lang.String", actualResult);
    }
}