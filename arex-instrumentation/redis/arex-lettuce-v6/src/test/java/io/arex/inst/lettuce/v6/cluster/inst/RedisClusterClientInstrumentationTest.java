package io.arex.inst.lettuce.v6.cluster.inst;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.redis.common.RedisConnectionManager;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RedisClusterClientInstrumentationTest {

    static RedisClusterClientInstrumentation instrumentation;

    static CompletableFuture connectionFuture;
    static Iterable<RedisURI> redisURIs;

    @BeforeAll
    static void setUp() {
        instrumentation = new RedisClusterClientInstrumentation();
        StatefulRedisClusterConnection statefulRedisClusterConnection = Mockito.mock(StatefulRedisClusterConnection.class);
        connectionFuture = new CompletableFuture();
        connectionFuture.complete(statefulRedisClusterConnection);
        List<RedisURI> redisURIList = new ArrayList<>();
        redisURIList.add(RedisURI.builder().withHost("127.0.0.1").withPort(6379).build());
        redisURIList.add(RedisURI.builder().withHost("127.0.0.1").withPort(6380).build());
        redisURIList.add(RedisURI.builder().withHost("127.0.0.1").withPort(6381).build());
        redisURIList.add(RedisURI.builder().withHost("127.0.0.1").withPort(6382).build());
        redisURIs = redisURIList;
    }

    @AfterAll
    static void tearDown() {
        instrumentation = null;
        connectionFuture = null;
    }

    @Test
    void typeMatcher() {
        assertTrue(instrumentation.typeMatcher().matches(TypeDescription.ForLoadedType.of(RedisClusterClient.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, instrumentation.methodAdvices().size());
    }

    @Test
    void onExit() {
        RedisClusterClientInstrumentation.NewStatefulRedisConnectionAdvice.onExit(connectionFuture, redisURIs);
        connectionFuture.thenAccept(connection -> {
            String redisURI = RedisConnectionManager.getRedisUri(connection.hashCode());
            assertEquals("RedisURI [host='127.0.0.1', port=6379]", redisURI);
        });

    }
}
