package io.arex.inst.database.mybatis3;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
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
    void onEnter() throws SQLException {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        assertFalse(ExecutorInstrumentation.QueryAdvice.onMethodEnter(null, null, null, null, MockResult.success("mock")));
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        assertFalse(ExecutorInstrumentation.UpdateAdvice.onMethodEnter(null, null, null, null, new SimpleExecutor(null, null)));
        assertFalse(ExecutorInstrumentation.UpdateAdvice.onMethodEnter(null, null, null, null, new BatchExecutor(null, null)));
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
            Mockito.when(RepeatedCollectManager.exitAndValidate(ArgumentMatchers.anyString())).thenReturn(false);
        };
        Runnable needRecord = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate(ArgumentMatchers.anyString())).thenReturn(true);
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
        ExecutorInstrumentation.UpdateAdvice.onExit(null, null, null, null, extractor, mockResult, new SimpleExecutor(null, null));
        assertTrue(predicate.test(mockResult));
    }

    static Stream<Arguments> onUpdateExitCase() {
        Runnable needReplay = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate(ArgumentMatchers.anyString())).thenReturn(false);
        };
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate(ArgumentMatchers.anyString())).thenReturn(true);
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

    @Test
    void batchUpdateOnEnter() {

        List<BatchResult> batchResultList = new ArrayList<>();
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        assertFalse(ExecutorInstrumentation.BatchUpdateAdvice.onEnter(null, null, null, null, null));

        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Builder builder = new Builder(new Configuration(), "", new StaticSqlSource(new Configuration(), "testSql"), null);
        assertTrue(ExecutorInstrumentation.BatchUpdateAdvice.onEnter(batchResultList, null, builder.build(),
                builder.build(), "param1"));
        Assertions.assertEquals(1, batchResultList.size());
        assertTrue(ExecutorInstrumentation.BatchUpdateAdvice.onEnter(batchResultList, "testSql", builder.build(),
                builder.build(), "param2"));
        Assertions.assertEquals(1, batchResultList.size());
        Assertions.assertEquals(2, batchResultList.get(0).getParameterObjects().size());
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        assertFalse(ExecutorInstrumentation.BatchUpdateAdvice.onEnter(batchResultList, null, builder.build(),
                builder.build(), "param1"));
    }

    @Test
    void batchFlushEnter() {
        List<BatchResult> batchResultList = new ArrayList<>();
        assertFalse(ExecutorInstrumentation.BatchFlushAdvice.onEnter(batchResultList, false, null, null));
        BatchResult batchResult = new BatchResult(null, null);
        batchResult.addParameterObject("test");
        batchResultList.add(batchResult);
        assertFalse(ExecutorInstrumentation.BatchFlushAdvice.onEnter(batchResultList, true, null, null));
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        ArrayList<DatabaseExtractor> extractorList = new ArrayList<>();
        assertFalse(ExecutorInstrumentation.BatchFlushAdvice.onEnter(batchResultList, false, extractorList, null));

        MockResult mockResult = MockResult.success(null);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        assertTrue(ExecutorInstrumentation.BatchFlushAdvice.onEnter(batchResultList, false, extractorList, mockResult));
    }

    @Test
    void testFlushExit() {
        List<BatchResult> batchResultList = new ArrayList<>();
        BatchResult batchResult = new BatchResult(null, null);
        batchResult.addParameterObject("test");
        batchResultList.add(batchResult);
        MockResult mockResult = MockResult.success(batchResultList);
        String currentSql = "testSql";
        ExecutorInstrumentation.BatchFlushAdvice.onExit(batchResultList, currentSql, batchResultList, true, null, null, null);
        Assertions.assertEquals("testSql", currentSql);

        ExecutorInstrumentation.BatchFlushAdvice.onExit(batchResultList, currentSql, batchResultList, false, null, null, mockResult);
        Assertions.assertEquals(0, batchResultList.size());

        batchResultList.add(batchResult);
        List<DatabaseExtractor> extractorList = Arrays.asList(new DatabaseExtractor(null, null, null));
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        ExecutorInstrumentation.BatchFlushAdvice.onExit(batchResultList, currentSql, batchResultList, false, null, extractorList, null);
        Assertions.assertDoesNotThrow(() -> ExecutorInstrumentation.BatchFlushAdvice.onExit(batchResultList, currentSql, batchResultList, false, null, null, null));
        Assertions.assertDoesNotThrow(() -> ExecutorInstrumentation.BatchFlushAdvice.onExit(batchResultList, currentSql, batchResultList, false, null, new ArrayList<>(), null));

    }

}
