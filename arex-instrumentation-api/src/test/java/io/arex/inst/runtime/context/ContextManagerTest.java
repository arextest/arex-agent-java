package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.TraceContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ContextManagerTest {

    @BeforeAll
    static void setUp() {
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("currentContextCase")
    void currentContext(boolean createIfAbsent, String caseId, Runnable mocker, Predicate<ArexContext> predicate) {
        try (MockedStatic<TraceContextManager> traceContextManager = Mockito.mockStatic(TraceContextManager.class)) {
            mocker.run();
            assertTrue(predicate.test(ContextManager.currentContext(createIfAbsent, caseId)));
        }
    }

    static Stream<Arguments> currentContextCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(TraceContextManager.get(any(Boolean.class))).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(TraceContextManager.get(any(Boolean.class))).thenReturn("mock2");
        };
        Predicate<ArexContext> predicate1 = Objects::isNull;
        Predicate<ArexContext> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(true, "mock", emptyMocker, predicate1),
                arguments(true, null, emptyMocker, predicate1),
                arguments(true, null, mocker1, predicate2),
                arguments(true, "mock", mocker2, predicate2),
                arguments(false, null, mocker2, predicate2)
        );
    }

    @Test
    void setAttachment() {
        try (MockedStatic<TraceContextManager> traceContextManager = Mockito.mockStatic(TraceContextManager.class)) {
            Mockito.when(TraceContextManager.get(any(Boolean.class))).thenReturn("record-id");
            ContextManager.currentContext(true, null);
            ContextManager.setAttachment("attachment-key", "attachment-value");
            assertEquals("attachment-value", ContextManager.currentContext().getAttachment("attachment-key"));
        }
    }
}
