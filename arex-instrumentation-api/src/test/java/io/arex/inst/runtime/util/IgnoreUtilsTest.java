package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class IgnoreUtilsTest {

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
    void ignoreMockResult(Runnable mocker, String serviceKey, String operationKey, Predicate<Boolean> predicate) {
        mocker.run();
        boolean result = IgnoreUtils.ignoreMockResult(serviceKey, operationKey);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> ignoreMockResultCase() {
        Runnable emptyMocker = () -> {};
        ArexContext context = ArexContext.of("mock");
        Set<String> operations = new HashSet<>();
        Runnable mocker1 = () -> {
            context.setExcludeMockTemplate(Collections.singletonMap("service", operations));
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        Runnable mocker2 = () -> {
            operations.add("operation");
            context.setExcludeMockTemplate(Collections.singletonMap("service", operations));
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(emptyMocker, null, null, predicate1),
                arguments(emptyMocker, "service", null, predicate1),
                arguments(mocker1, "service1", null, predicate1),
                arguments(mocker1, "service", "operation", predicate2),
                arguments(mocker2, "service", "operation", predicate2),
                arguments(mocker2, "service", "operation1", predicate1)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @EmptySource
    @ValueSource(strings = {"/api", "/api/v1/get/order", "/api/v2/_info", "/api/v3", "/api/v4/query", "*"})
    void ignoreOperation(String targetName) {
        final ConfigBuilder configBuilder = ConfigBuilder.create("mock")
                .excludeServiceOperations(Sets.newSet("/api", "/api/v1/*", "*_info", "*"));
        // includeServiceOperations empty
        configBuilder.build();
        if (StringUtil.isEmpty(targetName) ||
            "/api/v3".equals(targetName) ||
            "/api/v4/query".equals(targetName)) {
            assertFalse(IgnoreUtils.ignoreOperation(targetName));
        } else {
            assertTrue(IgnoreUtils.ignoreOperation(targetName));
        }

        // includeServiceOperations not empty
        configBuilder.addProperty("includeServiceOperations", "/api,/api/v1/*,*_info,*/v4/*");
        configBuilder.build();
        if ("/api/v3".equals(targetName) || "*".equals(targetName)) {
            assertTrue(IgnoreUtils.ignoreOperation(targetName));
        } else {
            assertFalse(IgnoreUtils.ignoreOperation(targetName));
        }

    }

    @Test
    void ignoreOperation_excludePathList() {
        ConfigBuilder.create("mock").build();
        assertFalse(IgnoreUtils.ignoreOperation("api/v3"));
    }

    @Test
    void invalidOperation() {
        IgnoreUtils.addInvalidOperation("testClass.testMethod");
        assertTrue(IgnoreUtils.invalidOperation("testClass.testMethod"));
    }
}
