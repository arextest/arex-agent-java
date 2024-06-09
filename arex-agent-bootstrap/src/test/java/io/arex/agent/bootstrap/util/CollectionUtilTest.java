package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionUtilTest {

    @Test
    void isEmpty() {
        assertTrue(CollectionUtil.isEmpty(null));
        assertTrue(CollectionUtil.isEmpty(Collections.emptyList()));
    }

    @Test
    void isNotEmpty() {
        assertTrue(CollectionUtil.isNotEmpty(Collections.singleton("test")));
    }

    @Test
    void emptyList() {
        assertInstanceOf(ArrayList.class, CollectionUtil.emptyList());
    }

    @Test
    void newArrayList() {
        List<String> actualResult = CollectionUtil.newArrayList(null);
        assertInstanceOf(ArrayList.class, actualResult);

        actualResult = CollectionUtil.newArrayList("test");
        assertInstanceOf(ArrayList.class, actualResult);
    }

    @Test
    void newHashSet() {
        Set<String> actualResult = CollectionUtil.newHashSet(null);
        assertInstanceOf(HashSet.class, actualResult);

        actualResult = CollectionUtil.newHashSet("test");
        assertInstanceOf(HashSet.class, actualResult);
    }

    @ParameterizedTest
    @MethodSource("splitCase")
    void split(List<String> originalList, int splitCount, Predicate<List<List<String>>> predicate) {
        assertTrue(predicate.test(CollectionUtil.split(originalList, splitCount)));
    }

    static Stream<Arguments> splitCase() {
        Supplier<List<String>> lessSplitCountList = () -> CollectionUtil.newArrayList("mock");
        Supplier<List<String>> normalSplitCountList = () -> CollectionUtil.newArrayList("mock1", "mock2");

        Predicate<List<List<String>>> empty = CollectionUtil::isEmpty;
        Predicate<List<List<String>>> notEmpty = CollectionUtil::isNotEmpty;

        return Stream.of(
                arguments(null, 1, empty),
                arguments(lessSplitCountList.get(), 2, notEmpty),
                arguments(normalSplitCountList.get(), 2, notEmpty)
        );
    }

    @Test
    void filterNull() {
        List<String> actualResult = CollectionUtil.filterNull(null);
        assertEquals(0, actualResult.size());

        actualResult = CollectionUtil.filterNull(CollectionUtil.newArrayList("mock"));
        assertEquals(1, actualResult.size());
    }
}
