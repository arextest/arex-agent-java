package io.arex.inst.lettuce.v5.standalone.inst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.lettuce.v5.standalone.inst.RedisClientInstrumentation;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RedisClientInstrumentationTest {
    static RedisClientInstrumentation instrumentation;

    static StatefulRedisConnectionImpl connection;

    @BeforeAll
    static void setUp() {
        instrumentation = new RedisClientInstrumentation();
        connection = Mockito.mock(StatefulRedisConnectionImpl.class);
    }

    @AfterAll
    static void tearDown() {
        instrumentation = null;
        connection = null;
    }

    @Test
    void typeMatcher() {
        assertTrue(instrumentation.typeMatcher().matches(TypeDescription.ForLoadedType.of(RedisClient.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, instrumentation.methodAdvices().size());
    }

    @Test
    void onExit() {
        RedisClientInstrumentation.NewStatefulRedisConnectionAdvice.onExit(connection, RedisURI.create("127.0.0.1", 6379));
        String redisURI = RedisConnectionManager.getRedisUri(connection.hashCode());
        assertEquals("RedisURI [host='127.0.0.1', port=6379]", redisURI);
    }
}
