package io.arex.inst.httpclient.apache.async;

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

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalHttpAsyncClientInstrumentationTest {
    static InternalHttpAsyncClientInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new InternalHttpAsyncClientInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(FutureCallbackWrapper.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void adviceClassNames() {
        assertNotNull(target.adviceClassNames());
    }

    @ParameterizedTest
    @MethodSource("onEnterCase")
    void onEnter(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        boolean result = InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onEnter(null, null, null);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> onEnterCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            FutureCallbackWrapper wrapper = Mockito.mock(FutureCallbackWrapper.class);
            Mockito.when(FutureCallbackWrapper.get(any(), any())).thenReturn(wrapper);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        };

        Predicate<Boolean> predicate1 = result -> !result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate1)
        );
    }

    @Test
    void onExit() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        FutureCallbackWrapper wrapper = Mockito.mock(FutureCallbackWrapper.class);
        Mockito.when(wrapper.replay()).thenReturn(true);
        InternalHttpAsyncClientInstrumentation.ExecuteAdvice.onExit(wrapper, null);
        verify(wrapper).replay();
    }
}