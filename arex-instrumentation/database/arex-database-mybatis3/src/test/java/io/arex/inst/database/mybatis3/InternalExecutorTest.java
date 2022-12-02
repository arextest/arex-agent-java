package io.arex.inst.database.mybatis3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InternalExecutorTest {
    static InternalExecutor target = null;
    static DatabaseExtractor extractor = null;
    static MappedStatement mappedStatement = null;
    static BoundSql boundSql = null;

    @BeforeAll
    static void setUp() {
        target = new InternalExecutor();
        extractor = Mockito.mock(DatabaseExtractor.class);
        Mockito.when(extractor.getSql()).thenReturn("insert into");
        Mockito.when(extractor.getKeyHolder()).thenReturn("key,val");
        mappedStatement = Mockito.mock(MappedStatement.class);
        Mockito.when(mappedStatement.getKeyProperties()).thenReturn(new String[]{"key"});
        boundSql = Mockito.mock(BoundSql.class);
        Mockito.when(boundSql.getSql()).thenReturn("insert into");
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(AsyncHttpClientUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void replay() throws SQLException {
        assertNotNull(target.replay(mappedStatement, new Object(), boundSql, "insert"));
    }

    @Test
    void testReplay() throws SQLException {
        assertNull(target.replay(extractor, mappedStatement, null));
        assertNull(target.replay(extractor, mappedStatement, new Object()));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("recordCase")
    void record(Throwable throwable) {
        target.record(mappedStatement, new Object(), boundSql, null, throwable, "insert");
    }

    static Stream<Arguments> recordCase() {
        return Stream.of(
                arguments(new SQLException())
        );
    }

    @ParameterizedTest
    @MethodSource("testRecordCase")
    void testRecord(Throwable throwable, Invoker invoker) {
        try (MockedConstruction<Reflector> mocked = Mockito.mockConstruction(Reflector.class, (mock, context) -> {
            Mockito.when(mock.getGetInvoker(any())).thenReturn(invoker);
        })) {
            target.record(extractor, mappedStatement, new Object(), null, throwable);
        }
    }

    static Stream<Arguments> testRecordCase() throws InvocationTargetException, IllegalAccessException {
        Invoker invoker1 = Mockito.mock(Invoker.class);
        Invoker invoker2 = Mockito.mock(Invoker.class);
        Mockito.when(invoker2.invoke(any(), any())).thenReturn("");
        return Stream.of(
                arguments(new NullPointerException(), invoker1),
                arguments(new SQLException(), invoker1),
                arguments(new SQLException(), invoker2)
        );
    }
}