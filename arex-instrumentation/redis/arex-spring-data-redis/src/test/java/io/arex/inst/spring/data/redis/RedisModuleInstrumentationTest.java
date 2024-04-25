package io.arex.inst.spring.data.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RedisModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        RedisModuleInstrumentation instrumentation = new RedisModuleInstrumentation();
        assertEquals(instrumentation.instrumentationTypes().size(), 2);
    }
}
