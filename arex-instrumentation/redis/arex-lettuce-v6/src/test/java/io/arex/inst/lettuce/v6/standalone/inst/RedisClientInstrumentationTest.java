package io.arex.inst.lettuce.v6.standalone.inst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.redis.common.RedisConnectionManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;
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
        RedisClientInstrumentation.NewStatefulRedisConnectionAdvice.onExit(connection, RedisURI.builder().withHost("127.0.0.1").withPort(6379).build());
        String redisURI = RedisConnectionManager.getRedisUri(connection.hashCode());
        assertEquals("redis://127.0.0.1", redisURI);
    }
}
