package io.arex.inst.database.mybatis3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.context.RepeatedCollectManager;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertFalse(ExecutorInstrumentation.QueryAdvice.onMethodEnter());
        assertFalse(ExecutorInstrumentation.Query1Advice.onMethodEnter());
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        assertFalse(ExecutorInstrumentation.UpdateAdvice.onMethodEnter(null, null, null));
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker) throws SQLException {
        mocker.run();
        ExecutorInstrumentation.QueryAdvice.onExit(null, null, null, null, null);
        ExecutorInstrumentation.Query1Advice.onExit(null, null, null, null);
    }

    static Stream<Arguments> onExitCase() {
        Runnable needReplay = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable exitAndValidate = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        return Stream.of(
                arguments(needReplay),
                arguments(exitAndValidate),
                arguments(needRecord)
        );
    }

    @ParameterizedTest
    @MethodSource("onUpdateExitCase")
    void onUpdateExit(Runnable mocker, DatabaseExtractor extractor) throws SQLException {
        mocker.run();
        ExecutorInstrumentation.UpdateAdvice.onExit(null, null, null, extractor, null);
    }

    static Stream<Arguments> onUpdateExitCase() {
        Runnable needReplay = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(false);
        };
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        DatabaseExtractor extractor = new DatabaseExtractor("", "");
        return Stream.of(
                arguments(needReplay, null),
                arguments(exitAndValidate, extractor),
                arguments(needRecord, extractor)
        );
    }
}