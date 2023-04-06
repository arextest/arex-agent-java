package io.arex.inst.jedis.v2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JedisFactoryInstrumentationTest {
    static JedisFactoryInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new JedisFactoryInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
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
    void onEnter() {
        HostAndPort hostAndPort = Mockito.mock(HostAndPort.class);
        Mockito.when(hostAndPort.getPort()).thenReturn(0);
        AtomicReference<HostAndPort> hostAndPortAR = new AtomicReference<>(hostAndPort);
        assertNotNull(JedisFactoryInstrumentation.MakeObjectAdvice.onEnter(
                hostAndPortAR, 0, 0, false, null, null, null));
    }

    @Test
    void onExit() throws Exception {
        JedisFactoryInstrumentation.MakeObjectAdvice.onExit(null, null, null, null, null);
        Jedis jedis = Mockito.mock(Jedis.class);
        JedisFactoryInstrumentation.MakeObjectAdvice.onExit(jedis, "mock", 1, "mock", null);
        Mockito.doThrow(new JedisException("")).when(jedis).connect();
        assertThrows(JedisException.class, () -> JedisFactoryInstrumentation.MakeObjectAdvice.onExit(
                jedis, "mock", 1, "mock", null));
    }
}