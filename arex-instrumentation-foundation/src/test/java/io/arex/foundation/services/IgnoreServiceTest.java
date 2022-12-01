package io.arex.foundation.services;

import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class IgnoreServiceTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("ignoreMockResultCase")
    void ignoreMockResult(Runnable mocker, String serviceKey, Predicate<Boolean> predicate) {
        mocker.run();
        boolean result = IgnoreService.ignoreMockResult(serviceKey, "operation");
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> ignoreMockResultCase() {
        Runnable emptyMocker = () -> {};
        ArexContext context = Mockito.mock(ArexContext.class);
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        Map<String, Set<String>> excludeMockTemplate = new HashMap<>();
        Runnable mocker2 = () -> {
            excludeMockTemplate.put("service", null);
            Mockito.when(context.getExcludeMockTemplate()).thenReturn(excludeMockTemplate);
        };
        Runnable mocker3 = () -> {
            Set<String> operationSet = new HashSet<>();
            operationSet.add("operation");
            excludeMockTemplate.put("service", operationSet);
            Mockito.when(context.getExcludeMockTemplate()).thenReturn(excludeMockTemplate);
        };
        Runnable mocker4 = () -> {
            Set<String> operationSet = new HashSet<>();
            operationSet.add("mock");
            excludeMockTemplate.put("service", operationSet);
            Mockito.when(context.getExcludeMockTemplate()).thenReturn(excludeMockTemplate);
        };

        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(emptyMocker, null, predicate1),
                arguments(emptyMocker, "service", predicate1),
                arguments(mocker1, "service", predicate1),
                arguments(mocker2, "service", predicate2),
                arguments(mocker3, "service", predicate2),
                arguments(mocker4, "service", predicate1)
        );
    }
}