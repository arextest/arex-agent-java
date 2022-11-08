package io.arex.inst.dynamic;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DynamicClassInstrumentationTest {
    static DynamicClassInstrumentation target = null;
    static String dynamicClass = "java.lang.System";

    @BeforeAll
    static void setUp() {
        target = new DynamicClassInstrumentation(dynamicClass);
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @ParameterizedTest()
    @MethodSource("methodAdvicesCase")
    void methodAdvices(String dynamicClass, Predicate<List<MethodInstrumentation>> predicate) {
        target = new DynamicClassInstrumentation(dynamicClass);
        List<MethodInstrumentation> result = target.methodAdvices();
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> methodAdvicesCase() {
        Predicate<List<MethodInstrumentation>> predicate = result -> !result.isEmpty();

        return Stream.of(
                arguments(null, predicate),
                arguments(dynamicClass, predicate)
        );
    }

    @Test
    void onEnter() {
        assertFalse(DynamicClassInstrumentation.MethodAdvice.onEnter(dynamicClass));
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker) {
        mocker.run();
        DynamicClassInstrumentation.MethodAdvice.onExit(
                "java.lang.System", "getenv", new Object[]{"java.lang.String"}, null);
    }

    static Stream<Arguments> onExitCase() {
        Runnable mockerNeedReplay = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("test-case-id"));
        };
        Runnable mockerNeedRecord = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        return Stream.of(
                arguments(mockerNeedReplay),
                arguments(mockerNeedRecord)
        );
    }

}