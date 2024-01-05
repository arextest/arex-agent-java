package io.arex.inst.database.hibernate;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.runtime.service.DataService;
import java.util.concurrent.atomic.AtomicReference;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith(MockitoExtension.class)
class AbstractEntityPersisterInstrumentationTest {
    static AbstractEntityPersisterInstrumentation target = null;
    static DataService dataService = DataService.INSTANCE;

    @BeforeAll
    static void setUp() {
        target = new AbstractEntityPersisterInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
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
    void onInsertEnter() {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(false, null));
        })) {
            assertTrue(AbstractEntityPersisterInstrumentation.InsertAdvice.onEnter(null, null, null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("onInsertExitCase")
    void onInsertExit(Runnable mocker, Throwable throwable, MockResult mockResult, Predicate<MockResult> predicate) {
        mocker.run();
        DatabaseExtractor extractor = Mockito.mock(DatabaseExtractor.class);
        AbstractEntityPersisterInstrumentation.InsertAdvice.onExit(null, throwable, mockResult, extractor);
        assertTrue(predicate.test(mockResult));
    }

    static Stream<Arguments> onInsertExitCase() {
        Runnable emptyMocker = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(false);
        };
        Runnable exitAndValidate = () -> {
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        };
        Runnable needRecord = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };

        Serializable serializable = Mockito.mock(Serializable.class);
        Predicate<MockResult> predicate1 = Objects::isNull;
        Predicate<MockResult> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, null, null, predicate1),
                arguments(exitAndValidate, null, MockResult.success(serializable), predicate2),
                arguments(exitAndValidate, null, MockResult.success(new HibernateException("")), predicate2),
                arguments(needRecord, new HibernateException(""), null, predicate1),
                arguments(needRecord, null, null, predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("onUpdateOrInsertEnterCase")
    void onUpdateOrInsertEnter(Runnable mocker, MockResult mockResult, Predicate<Integer> predicate) {
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(mockResult);
        })) {
            mocker.run();
            int result = AbstractEntityPersisterInstrumentation.UpdateOrInsertAdvice.onEnter(null, null, null, MockResult.success("mock"));
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> onUpdateOrInsertEnterCase() {
        Runnable needReplay = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Predicate<Integer> predicate1 = result -> result == 0;
        Predicate<Integer> predicate2 = result -> result == 1;
        return Stream.of(
                arguments(needReplay, MockResult.success("mock"), predicate2),
                arguments(needReplay, null, predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("onUpdateOrInsertExitCase")
    void onUpdateOrInsertExit(Runnable recordType, Object mockResult) {
        AtomicReference<DatabaseExtractor> mo = new AtomicReference<>();
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            mo.set(mock);
        })){
            recordType.run();
            Mockito.verify(mo.get(), Mockito.times(1)).recordDb(isA(mockResult.getClass()));
        }
    }

    static Stream<Arguments> onUpdateOrInsertExitCase() {
        Runnable recordThrowable = () -> AbstractEntityPersisterInstrumentation.UpdateOrInsertAdvice.onExit(null, null, new NullPointerException(), MockResult.success(2));
        Runnable recordNormalResponse = () -> AbstractEntityPersisterInstrumentation.UpdateOrInsertAdvice.onExit(null, null, null, MockResult.success(2));
        return Stream.of(
                arguments(recordThrowable, new NullPointerException()),
                arguments(recordNormalResponse, 0)
        );
    }

    @Test
    void updateOrInsertExitReplayThrowable() {
        MockResult mock = Mockito.mock(MockResult.class);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        Throwable throwable = new Throwable();
        Mockito.doReturn(true).when(mock).notIgnoreMockResult();
        Mockito.doReturn(throwable).when(mock).getThrowable();
        AbstractEntityPersisterInstrumentation.UpdateOrInsertAdvice.onExit(null, null, throwable, mock);
        Mockito.verify(mock, Mockito.times(2)).getThrowable();
    }

    @ParameterizedTest
    @MethodSource("onDeleteEnterCase")
    void onDeleteEnter(Runnable judgeReplay, MockResult mockResult, Predicate<Integer> predicate) {
        judgeReplay.run();
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(mockResult);
        })){
            assertTrue(predicate.test(AbstractEntityPersisterInstrumentation.DeleteAdvice.onEnter(null, null, null)));
        }
    }

    static Stream<Arguments> onDeleteEnterCase() {
        Runnable needReplay = () -> Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Runnable notNeedReplay = () -> Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Predicate<Integer> predicate1 = result -> result == 0;
        Predicate<Integer> predicate2 = result -> result == 1;
        return Stream.of(
                arguments(needReplay, MockResult.success(0), predicate2),
                arguments(needReplay, MockResult.success(true, 0), predicate1),
                arguments(notNeedReplay, MockResult.success(0), predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("onDeleteExitCase")
    void onDeleteExit(Runnable recordType, Object mockResult) {
        AtomicReference<DatabaseExtractor> mo = new AtomicReference<>();
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            mo.set(mock);
        })){
            recordType.run();
            Mockito.verify(mo.get(), Mockito.times(1)).recordDb(isA(mockResult.getClass()));
        }
    }

    static Stream<Arguments> onDeleteExitCase() {
        Runnable recordThrowable = () -> AbstractEntityPersisterInstrumentation.DeleteAdvice.onExit(null, null, new NullPointerException(), MockResult.success(2));
        Runnable recordNormalResponse = () -> AbstractEntityPersisterInstrumentation.DeleteAdvice.onExit(null, null, null, MockResult.success(2));
        return Stream.of(
                arguments(recordThrowable, new NullPointerException()),
                arguments(recordNormalResponse, 0)
        );
    }

    @Test
    void deleteExitReplayThrowable() {
        MockResult mock = Mockito.mock(MockResult.class);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        Throwable throwable = new Throwable();
        Mockito.doReturn(true).when(mock).notIgnoreMockResult();
        Mockito.doReturn(throwable).when(mock).getThrowable();
        AbstractEntityPersisterInstrumentation.DeleteAdvice.onExit(null, null, throwable, mock);
        Mockito.verify(mock, Mockito.times(2)).getThrowable();
    }
}