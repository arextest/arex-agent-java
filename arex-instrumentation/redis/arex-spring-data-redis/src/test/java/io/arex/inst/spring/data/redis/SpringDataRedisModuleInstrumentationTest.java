package io.arex.inst.spring.data.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SpringDataRedisModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        SpringDataRedisModuleInstrumentation instrumentation = new SpringDataRedisModuleInstrumentation();
        assertEquals(instrumentation.instrumentationTypes().size(),1);
    }
}
