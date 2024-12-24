package io.arex.inst.jedis.v3;

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
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.HashMap;
import java.util.Map;
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

    @Test
    void callWithEmptyKeysValuesReturnsDefault() {
        long result = target.msetnx( new String[]{});
        assertEquals(0, result);
    }

    @Test
    void callWithTwoKeysValuesReturnsCallableResult() {
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        Mockito.when(client.getIntegerReply()).thenReturn(1L);
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

    @Test
    void testApi() {
        Map<byte[], byte[]> hash = new HashMap<>();
        assertDoesNotThrow(() -> target.hset("key".getBytes(), hash));
        Map<String, String> hash1 = new HashMap<>();
        assertDoesNotThrow(() -> target.set("key", "value"));
        assertDoesNotThrow(() -> target.set("key", "value", new SetParams().ex(10)));
        assertDoesNotThrow(() -> target.get("key"));
        assertDoesNotThrow(() -> target.getDel("key"));
        assertDoesNotThrow(() -> target.getEx("key", new GetExParams().ex(10)));
        assertDoesNotThrow(() -> target.exists("key1", "key2"));
        assertDoesNotThrow(() -> target.exists("key"));
        assertDoesNotThrow(() -> target.del("key1", "key2"));
        assertDoesNotThrow(() -> target.del("key"));
        assertDoesNotThrow(() -> target.unlink("key1", "key2"));
        assertDoesNotThrow(() -> target.unlink("key"));
        assertDoesNotThrow(() -> target.type("key"));
        assertDoesNotThrow(() -> target.keys("key"));
        assertDoesNotThrow(() -> target.rename("key1".getBytes(), "key2".getBytes()));
        assertDoesNotThrow(() -> target.renamenx("key1".getBytes(), "key2".getBytes()));
        assertDoesNotThrow(() -> target.expire("key", 1L));
        assertDoesNotThrow(() -> target.expireAt("key", 1L));
        assertDoesNotThrow(() -> target.ttl("key"));
        assertDoesNotThrow(() -> target.getSet("key", "value"));
        assertDoesNotThrow(() -> target.mget("key1", "key2"));
        assertDoesNotThrow(() -> target.setnx("key", "value"));
        assertDoesNotThrow(() -> target.setex("key", 1L, "value"));
        assertDoesNotThrow(() -> target.mset("key1", "value1", "key2", "value2"));
        assertDoesNotThrow(() -> target.msetnx("key1", "value1", "key2", "value2"));
        assertDoesNotThrow(() -> target.decrBy("key", 1L));
        assertDoesNotThrow(() -> target.decr("key"));
        assertDoesNotThrow(() -> target.incrBy("key", 1L));
        assertDoesNotThrow(() -> target.incrByFloat("key", 1.1D));
        assertDoesNotThrow(() -> target.incr("key"));
        assertDoesNotThrow(() -> target.append("key", "value"));
        assertDoesNotThrow(() -> target.substr("key", 1, 2));
        assertDoesNotThrow(() -> target.hset("key", "field", "value"));
        assertDoesNotThrow(() -> target.hset("key", hash1));
        assertDoesNotThrow(() -> target.hget("key", "field"));
        assertDoesNotThrow(() -> target.hsetnx("key", "field", "value"));
        assertDoesNotThrow(() -> target.hmset("key", hash1));
        assertDoesNotThrow(() -> target.hmget("key", "field"));
        assertDoesNotThrow(() -> target.hincrBy("key", "field", 1L));
        assertDoesNotThrow(() -> target.hincrByFloat("key", "field", 1.1D));
        assertDoesNotThrow(() -> target.hexists("key", "field"));
        assertDoesNotThrow(() -> target.hdel("key", "field"));
        assertDoesNotThrow(() -> target.hlen("key"));
        assertDoesNotThrow(() -> target.hkeys("key"));
        assertDoesNotThrow(() -> target.hvals("key"));
        assertDoesNotThrow(() -> target.hgetAll("key"));
        assertDoesNotThrow(() -> target.llen("key"));
        assertDoesNotThrow(() -> target.lrange("key", 1L, 2L));
        assertDoesNotThrow(() -> target.ltrim("key", 1L, 2L));
        assertDoesNotThrow(() -> target.lindex("key", 1L));
        assertDoesNotThrow(() -> target.lset("key", 1L, "value"));
        assertDoesNotThrow(() -> target.lpop("key"));
        assertDoesNotThrow(() -> target.rpop("key"));
        assertDoesNotThrow(() -> target.spop("key"));
        assertDoesNotThrow(() -> target.spop("key", 1L));
        assertDoesNotThrow(() -> target.scard("key"));
        assertDoesNotThrow(() -> target.sinter("key1", "key2"));
        assertDoesNotThrow(() -> target.sunion("key1", "key2"));
        assertDoesNotThrow(() -> target.sdiff("key1", "key2"));
        assertDoesNotThrow(() -> target.srandmember("key"));
        assertDoesNotThrow(() -> target.srandmember("key", 1));
        assertDoesNotThrow(() -> target.zcard("key"));
        assertDoesNotThrow(() -> target.strlen("key"));
        assertDoesNotThrow(() -> target.persist("key"));
        assertDoesNotThrow(() -> target.setrange("key", 1L, "value"));
        assertDoesNotThrow(() -> target.getrange("key", 1L, 2L));
        assertDoesNotThrow(() -> target.pttl("key"));
        assertDoesNotThrow(() -> target.psetex("key", 1L, "value"));
        assertDoesNotThrow(() -> target.substr("key".getBytes(), 1, 2));
        assertDoesNotThrow(() -> target.hset("key".getBytes(), "field".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.hset("key".getBytes(), hash));
        assertDoesNotThrow(() -> target.hget("key".getBytes(), "field".getBytes()));
        assertDoesNotThrow(() -> target.hdel("key".getBytes(), "field".getBytes()));
        assertDoesNotThrow(() -> target.hvals("key".getBytes()));
        assertDoesNotThrow(() -> target.hgetAll("key".getBytes()));
        assertDoesNotThrow(() -> target.pexpire("key", 1L));
        assertDoesNotThrow(() -> target.pexpireAt("key", 1L));
        assertDoesNotThrow(() -> target.get("key".getBytes()));
        assertDoesNotThrow(() -> target.exists("key1".getBytes(), "key2".getBytes()));
        assertDoesNotThrow(() -> target.exists("key".getBytes()));
        assertDoesNotThrow(() -> target.type("key".getBytes()));
        assertDoesNotThrow(() -> target.getSet("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.mget("key1".getBytes(), "key2".getBytes()));
        assertDoesNotThrow(() -> target.setnx("key".getBytes(), "value".getBytes()));
        assertDoesNotThrow(() -> target.setex("key".getBytes(), 1, "value".getBytes()));
        assertDoesNotThrow(() -> target.unlink("key1".getBytes(), "key2".getBytes()));
        assertDoesNotThrow(() -> target.unlink("key".getBytes()));
        assertDoesNotThrow(() -> target.rename("key", "key2"));
        assertDoesNotThrow(() -> target.renamenx("key", "key2"));
        assertDoesNotThrow(() -> target.ping());
        assertDoesNotThrow(() -> target.ping("message".getBytes()));
        assertDoesNotThrow(() -> target.ping("message"));
    }
}
