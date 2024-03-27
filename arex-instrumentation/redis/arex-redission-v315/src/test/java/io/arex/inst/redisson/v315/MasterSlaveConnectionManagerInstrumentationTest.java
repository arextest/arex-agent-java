package io.arex.inst.redisson.v315;

import io.arex.agent.bootstrap.util.ReflectUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.command.CommandSyncService;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveConnectionManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MasterSlaveConnectionManagerInstrumentationTest {
    static MasterSlaveConnectionManagerInstrumentation target = null;
    static ConnectionManager connectionManager;

    @BeforeAll
    static void setUp() {
        target = new MasterSlaveConnectionManagerInstrumentation();
        connectionManager = Mockito.mock(ConnectionManager.class);
        Mockito.mockStatic(ReflectUtil.class);
        Mockito.when(ReflectUtil.getMethod(ConnectionManager.class, "getConfig")).thenReturn(null);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
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
        assertTrue(MasterSlaveConnectionManagerInstrumentation.GetCommandExecutorAdvice.onEnter());
    }

    @Test
    void onExit() {
        MasterSlaveServersConfig masterSlaveServersConfig = new MasterSlaveServersConfig();
        Mockito.when(connectionManager.getConfig()).thenReturn(masterSlaveServersConfig);
        assertDoesNotThrow(() -> MasterSlaveConnectionManagerInstrumentation.GetCommandExecutorAdvice.onExit(
            Mockito.mock(MasterSlaveConnectionManager.class), new CommandSyncService(connectionManager)));
    }
}
