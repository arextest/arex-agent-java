package io.arex.inst.jedis.v4;

import io.arex.foundation.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

    @ParameterizedTest
    @MethodSource("callCase")
    void call(Runnable mocker, Predicate<String> predicate) {
        mocker.run();
        String result = target.hget("key", "field");
        assertTrue(predicate.test(result));
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