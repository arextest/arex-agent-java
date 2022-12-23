package io.arex.inst.jedis.v2;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class JedisWrapperTest {
    @Mock
    SSLSocketFactory factory;
    @Mock
    SSLParameters parameters;
    @Mock
    HostnameVerifier verifier;
    @InjectMocks
    JedisWrapper target = new JedisWrapper("", 0, 0, 0, false, factory, parameters, verifier);
    static Client client;

    @BeforeAll
    static void setUp() {
        Mockito.mockConstruction(Client.class, (mock, context) -> {
            client = mock;
        });
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        client = null;
        Mockito.clearAllCaches();
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
            Mockito.when(client.getBulkReply()).thenThrow(new NullPointerException());
        };
        Runnable mocker3 = () -> {
            Mockito.when(client.getBulkReply()).thenReturn("mock");
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