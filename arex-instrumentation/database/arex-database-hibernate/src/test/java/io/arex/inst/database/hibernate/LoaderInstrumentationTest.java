package io.arex.inst.database.hibernate;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import org.hibernate.HibernateException;
import org.hibernate.loader.Loader;
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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class LoaderInstrumentationTest {
    static LoaderInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new LoaderInstrumentation();
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
    void adviceClassNames() {
        assertNotNull(target.adviceClassNames());
    }

    @Test
    void onEnter() throws SQLException {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Loader loader = Mockito.mock(Loader.class);
        try (MockedConstruction<DatabaseExtractor> mocked = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success(false, null));
        })) {
            assertTrue(LoaderInstrumentation.QueryAdvice.onEnter(loader, null, null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker, HibernateException exception, MockResult mockResult, Predicate<HibernateException> predicate) throws SQLException {
        mocker.run();
        DatabaseExtractor extractor = Mockito.mock(DatabaseExtractor.class);
        LoaderInstrumentation.QueryAdvice.onExit(null, null, exception, null, mockResult, extractor);
        assertTrue(predicate.test(exception));
    }

    static Stream<Arguments> onExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable exitAndValidate = () -> Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        Runnable needRecord = () -> Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Predicate<HibernateException> predicate1 = Objects::isNull;
        Predicate<HibernateException> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, null, null, predicate1),
                arguments(exitAndValidate, null, MockResult.success(Collections.singletonList("mock")), predicate1),
                arguments(needRecord, new HibernateException(""), null, predicate2),
                arguments(needRecord, null, null, predicate1)
        );
    }
}