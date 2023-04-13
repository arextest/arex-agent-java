package io.arex.agent.bootstrap.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ArrayUtilsTest {

    @ParameterizedTest
    @MethodSource("addAllCase")
    void addAll(byte[] array1, byte[] array2, Predicate<byte[]> predicate) {
        byte[] joinedArray = ArrayUtils.addAll(array1, array2);
        assertTrue(predicate.test(joinedArray));
    }

    static Stream<Arguments> addAllCase() {
        Predicate<byte[]> predicate1 = bytes -> bytes.length == 0;
        Predicate<byte[]> predicate2 = bytes -> bytes.length == 1;
        Predicate<byte[]> predicate3 = bytes -> bytes.length == 2;
        return Stream.of(
                arguments(null, null, predicate1),
                arguments(null, new byte[]{2}, predicate2),
                arguments(new byte[]{1}, null, predicate2),
                arguments(new byte[]{1}, new byte[]{2}, predicate3)
        );
    }

    @Test
    void isEmpty() {
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
    }
}