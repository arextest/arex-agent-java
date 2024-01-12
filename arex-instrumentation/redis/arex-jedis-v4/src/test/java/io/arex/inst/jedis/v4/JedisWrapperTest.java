package io.arex.inst.jedis.v4;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.*;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class JedisWrapperTest {
    @Mock
    JedisSocketFactory factory;
    @Mock
    JedisClientConfig config;
    @InjectMocks
    JedisWrapper target = new JedisWrapper(factory, config);
    static Connection connection;

    @BeforeAll
    static void setUp() {
        Mockito.mockConstruction(Connection.class, (mock, context) -> {
            connection = mock;
        });
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        connection = null;
        Mockito.clearAllCaches();
    }

    @Test
    void callWithEmptyKeysValuesReturnsDefault() {
        long result = target.msetnx( new String[]{});
        assertEquals(0, result);
    }

    @Test
    void callWithTwoKeysValuesReturnsCallableResult() {
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        Mockito.when(connection.executeCommand(any(CommandObject.class))).thenReturn(1L);
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
        })) {
            long result = target.msetnx("key", "value");
            assertEquals(1L, result);

            result = target.msetnx("key1", "value1", "key2", "value2", "key3", "value3");
            assertEquals(1L, result);

            result = target.exists("key1", "key2", "key3");
            assertEquals(1L, result);
        } catch (Exception e) {
            assertThrows(NullPointerException.class, () -> {
                throw e;
            });
        }
    }

    @ParameterizedTest
    @MethodSource("callCase")
    void call(Runnable mocker, Predicate<String> predicate) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            System.out.println("mock RedisExtractor");
            Mockito.when(mock.replay()).thenReturn(MockResult.success(null));
        })) {
            String result = target.hget("key", "field");
            assertTrue(predicate.test(result));
        } catch (Exception e) {
            assertThrows(NullPointerException.class, () -> {
                throw e;
            });
        }
    }

    static Stream<Arguments> callCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(connection.executeCommand(any(CommandObject.class))).thenThrow(new NullPointerException());
        };
        Runnable mocker3 = () -> {
            Mockito.when(connection.executeCommand(any(CommandObject.class))).thenReturn("mock");
        };
        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = "mock"::equals;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate2)
        );
    }
}
