package io.arex.inst.database.mybatis3;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import io.arex.inst.runtime.util.TypeUtil;
import org.apache.ibatis.binding.MapperMethod;
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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void replay() throws SQLException {
        DatabaseExtractor mock = Mockito.mock(DatabaseExtractor.class);
        MockResult mockResult = MockResult.success(true, null);
        Mockito.when(mock.replay()).thenReturn(mockResult);
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        Object parameterObject = new Object();
        assertNotNull(InternalExecutor.replay(mock, parameterObject));
        Mockito.verify(mock, Mockito.times(1)).replay();
    }

    @Test
    void testReplay() throws SQLException {
        assertNull(target.replay(extractor, mappedStatement, null));
        assertNull(target.replay(extractor, mappedStatement, new Object()));
        MapperMethod.ParamMap<Object> paramMap = new MapperMethod.ParamMap<>();
        assertNull(target.replay(extractor, mappedStatement, paramMap));

        Mockito.when(mappedStatement.getKeyProperties()).thenReturn(new String[]{"key.id"});
        paramMap.put("ms.id", "ms");
        assertNull(target.replay(extractor, mappedStatement, paramMap));
    }

    @ParameterizedTest
    @MethodSource("recordCase")
    void record(Throwable throwable, Object result) {
        DatabaseExtractor mockExtractor = Mockito.mock(DatabaseExtractor.class);
        Mockito.when(mappedStatement.getBoundSql(any())).thenReturn(boundSql);
        // no page
        target.record(mockExtractor, new Object(), result, throwable);
        Mockito.verify(mockExtractor, Mockito.times(1)).setPage(null);
        if (throwable != null) {
            Mockito.verify(mockExtractor, Mockito.times(1)).recordDb(throwable);
        } else {
            Mockito.verify(mockExtractor, Mockito.times(1)).recordDb(result);
        }

        // page
        Page<Object> page = new Page<>();
        Mockito.when(Serializer.serialize(page)).thenReturn("test-page");
        target.record(mockExtractor, page, result, null);
        Mockito.verify(mockExtractor, Mockito.times(1)).setPage("test-page");
    }

    static Stream<Arguments> recordCase() {
        return Stream.of(
                arguments(new SQLException(), StringUtil.EMPTY),
                arguments(null, StringUtil.EMPTY)
        );
    }

    @ParameterizedTest
    @MethodSource("testRecordCase")
    void testRecord(Throwable throwable, Invoker invoker, Object result) {
        try (MockedConstruction<Reflector> mocked = Mockito.mockConstruction(Reflector.class, (mock, context) -> {
            Mockito.when(mock.getGetInvoker(any())).thenReturn(invoker);
        })) {
            Mockito.when(mappedStatement.getKeyProperties()).thenReturn(new String[]{"key", "key2"});
            assertDoesNotThrow(() -> target.record(extractor, mappedStatement, new Object(), result, throwable));
        }
    }

    static Stream<Arguments> testRecordCase() throws InvocationTargetException, IllegalAccessException {
        Invoker invoker1 = Mockito.mock(Invoker.class);
        Invoker invoker2 = Mockito.mock(Invoker.class);
        Mockito.when(invoker2.invoke(any(), any())).thenReturn("");
        return Stream.of(
                arguments(new NullPointerException(), invoker1, StringUtil.EMPTY),
                arguments(null, invoker1, StringUtil.EMPTY),
                arguments(new SQLException(), invoker2, StringUtil.EMPTY)
        );
    }

    @Test
    void testRestorePage() throws Exception {
        Method restorePage = InternalExecutor.class.getDeclaredMethod("restorePage", DatabaseExtractor.class, Object.class);
        restorePage.setAccessible(true);
        DatabaseExtractor mockExtractor = Mockito.mock(DatabaseExtractor.class);

        // null
        restorePage.invoke(null, mockExtractor, null);
        Mockito.verify(mockExtractor, Mockito.times(1)).getPage();

        // empty
        Mockito.when(mockExtractor.getPage()).thenReturn(StringUtil.EMPTY);
        restorePage.invoke(null, mockExtractor, null);
        Mockito.verify(mockExtractor, Mockito.times(2)).getPage();

        IPage<Object> recordPage = new Page<>();
        recordPage.setTotal(10);

        Object parameterObject = null;

        Mockito.when(mockExtractor.getPage()).thenReturn("test-page");

        // no page
        parameterObject = new Object();
        restorePage.invoke(null, mockExtractor, parameterObject);

        // only page
        IPage<Object> originalPage = new Page<>();
        assertEquals(0, originalPage.getTotal());
        Type type = TypeUtil.forName(TypeUtil.getName(originalPage));
        Mockito.when(Serializer.deserialize("test-page", type)).thenReturn(recordPage);
        restorePage.invoke(null, mockExtractor, originalPage);
        assertEquals(10, originalPage.getTotal());

        // page in ParamMap
        originalPage = new Page<>();
        assertEquals(0, originalPage.getTotal());
        MapperMethod.ParamMap<Object> paramMap = new MapperMethod.ParamMap<>();
        paramMap.put("string", "string");
        paramMap.put("page", originalPage);
        restorePage.invoke(null, mockExtractor, paramMap);
        assertEquals(10, originalPage.getTotal());
        assertEquals(10, ((IPage)paramMap.get("page")).getTotal());
    }
}
