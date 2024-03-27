package io.arex.inst.redis.common.redisson;

import static org.junit.jupiter.api.Assertions.assertNull;

import io.arex.agent.bootstrap.util.ReflectUtil;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ConnectionManager;

@ExtendWith(MockitoExtension.class)
public class RedissonHelperTest {

    static ConnectionManager connectionManager;

    @BeforeAll
    static void setUp() {
        connectionManager = Mockito.mock(ConnectionManager.class);
        Mockito.mockStatic(ReflectUtil.class);
        Mockito.when(ReflectUtil.getMethod(ConnectionManager.class, "getConfig")).thenReturn(null);
    }
    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void testRedisUri() {
        MasterSlaveServersConfig masterSlaveServersConfig = new MasterSlaveServersConfig();
        Mockito.when(connectionManager.getConfig()).thenReturn(masterSlaveServersConfig);
        assertNull(RedissonHelper.getRedisUri(connectionManager));

        mockRedissionHelperSet();
        assertNull(RedissonHelper.getRedisUri(connectionManager));
    }

    @Test
    void testGetMasterSlaveServersConfig() {
        assertNull(RedissonHelper.getMasterSlaveServersConfig(connectionManager));
    }

    private void mockRedissionHelperSet() {
        // mock static final field
        try {
            Field field = RedissonHelper.class.getDeclaredField("masterSlaveServersConfigSet");
            field.setAccessible(true);
            Set<MasterSlaveServersConfig> set = new HashSet<>(1);
            set.add(new MasterSlaveServersConfig());
            field.set(null, set);
        } catch (Exception e) {
            // doNothing
        }
    }
}
