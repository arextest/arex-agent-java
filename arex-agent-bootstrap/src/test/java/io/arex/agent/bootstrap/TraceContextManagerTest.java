package io.arex.agent.bootstrap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @since 2024/1/15
 */
class TraceContextManagerTest {

    @Test
    void test() {
        TraceContextManager.init("test-ip");
        String get1 = TraceContextManager.get(true);
        String get2 = TraceContextManager.get();
        assertEquals(get1, get2);

        TraceContextManager.set(get2 + "-1");
        String get3 = TraceContextManager.get();
        assertEquals(get2 + "-1", get3);

        String get4 = TraceContextManager.remove();
        assertEquals(get3, get4);

        String get5 = TraceContextManager.generateId();
        assertTrue(get5.startsWith("AREX-test-ip-"));
    }
}
