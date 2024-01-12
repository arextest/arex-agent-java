package io.arex.inst.runtime.context;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @since 2024/1/12
 */
class RepeatedCollectManagerTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
    }
    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void test() {
        assertTrue(RepeatedCollectManager.validate());
        assertTrue(RepeatedCollectManager.exitAndValidate());

        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        RepeatedCollectManager.enter();
        RepeatedCollectManager.enter();
        assertFalse(RepeatedCollectManager.exitAndValidate());
        assertTrue(RepeatedCollectManager.exitAndValidate());
    }
}
