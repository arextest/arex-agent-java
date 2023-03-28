package io.arex.inst.jedis.v4;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

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
        try (MockedConstruction<JedisWrapper> mocked = Mockito.mockConstruction(JedisWrapper.class, (mock, context) -> {
        })) {
            assertNotNull(JedisFactoryInstrumentation.MakeObjectAdvice.onEnter(null, null));
        }
    }

    @Test
    void onExit() throws Exception {
        JedisFactoryInstrumentation.MakeObjectAdvice.onExit(null, null);
        Jedis jedis = Mockito.mock(Jedis.class);
        JedisFactoryInstrumentation.MakeObjectAdvice.onExit(jedis, null);
        Mockito.doThrow(new JedisException("")).when(jedis).connect();
        assertThrows(JedisException.class, () -> JedisFactoryInstrumentation.MakeObjectAdvice.onExit(jedis, null));
    }
}