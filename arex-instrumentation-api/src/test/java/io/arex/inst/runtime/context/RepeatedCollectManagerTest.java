package io.arex.inst.runtime.context;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.log.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @since 2024/1/12
 */
class RepeatedCollectManagerTest {
    private static MockedStatic<LogManager> logManagerMockedStatic;
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        logManagerMockedStatic = Mockito.mockStatic(LogManager.class);
    }
    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void test() {
        assertTrue(RepeatedCollectManager.validate());
        assertTrue(RepeatedCollectManager.exitAndValidate("test"));

        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        RepeatedCollectManager.enter();
        RepeatedCollectManager.enter();
        // enable debug
        ConfigBuilder.create("test").enableDebug(true).build();
        assertFalse(RepeatedCollectManager.exitAndValidate("test"));
        logManagerMockedStatic.verify(() -> LogManager.info("repeat", "test"));
        assertTrue(RepeatedCollectManager.exitAndValidate("test"));
    }
}
