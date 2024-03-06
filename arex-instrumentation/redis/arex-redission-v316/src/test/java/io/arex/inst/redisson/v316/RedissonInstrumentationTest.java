package io.arex.inst.redisson.v316;

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
import org.redisson.liveobject.core.RedissonObjectBuilder;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedissonInstrumentationTest {
    static RedissonInstrumentation target = null;
    static ConnectionManager connectionManager;
    static CommandSyncService commandSyncService;

    @BeforeAll
    static void setUp() {
        target =new RedissonInstrumentation();
        connectionManager = Mockito.mock(ConnectionManager.class);
        RedissonObjectBuilder redissonObjectBuilder = Mockito.mock(RedissonObjectBuilder.class);
        commandSyncService = new CommandSyncService(connectionManager,redissonObjectBuilder);
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
        assertTrue(RedissonInstrumentation.GetCommandExecutorAdvice.onEnter());
    }

    @Test
    void onExit() {
        MasterSlaveServersConfig masterSlaveServersConfig = new MasterSlaveServersConfig();
        Mockito.when(connectionManager.getConfig()).thenReturn(masterSlaveServersConfig);
        assertDoesNotThrow(() -> RedissonInstrumentation.GetCommandExecutorAdvice.onExit(
            commandSyncService, commandSyncService));
    }
}