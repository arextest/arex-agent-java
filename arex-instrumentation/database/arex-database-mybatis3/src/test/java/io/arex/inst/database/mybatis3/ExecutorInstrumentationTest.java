package io.arex.inst.database.mybatis3;

import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class ExecutorInstrumentationTest {
    static ExecutorInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ExecutorInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(InternalExecutor.class);
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

    @Test
    void onEnter() throws SQLException {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        assertFalse(ExecutorInstrumentation.QueryAdvice.onMethodEnter(null, null, null, MockResult.success("mock")));Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        assertFalse(ExecutorInstrumentation.UpdateAdvice.onMethodEnter(null, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker, MockResult mockResult, Predicate<MockResult> predicate) {
        mocker.run();
        ExecutorInstrumentation.QueryAdvice.onExit(null, null, null, null, null, mockResult);
        assertTrue(predicate.test(mockResult));
    }

    static Stream<Arguments> onExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(false);
        };
        Runnable needRecord = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Predicate<MockResult> predicate1 = Objects::isNull;
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, MockResult.success(new SQLException("mock exception")), predicate2),
                arguments(emptyMocker, MockResult.success(Collections.singletonList("mock")), predicate2),
                arguments(exitAndValidate, null, predicate1),
                arguments(needRecord, null, predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("onUpdateExitCase")
    void onUpdateExit(Runnable mocker, DatabaseExtractor extractor, MockResult mockResult, Predicate<MockResult> predicate) {
        mocker.run();
        ExecutorInstrumentation.UpdateAdvice.onExit(null, null, null, null, extractor, mockResult);
        assertTrue(predicate.test(mockResult));
    }

    static Stream<Arguments> onUpdateExitCase() {
        Runnable needReplay = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(false);
        };
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        DatabaseExtractor extractor = new DatabaseExtractor("", "", "");
        Predicate<MockResult> predicate1 = Objects::isNull;
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(needReplay, null, null, predicate1),
                arguments(exitAndValidate, extractor, MockResult.success(new SQLException("mock exception")), predicate2),
                arguments(exitAndValidate, extractor, MockResult.success(1), predicate2),
                arguments(needRecord, extractor, null, predicate1)
        );
    }
}