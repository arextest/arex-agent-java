package io.arex.inst.redis.common.redisson;

import io.arex.agent.bootstrap.util.ReflectUtil;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ConnectionManager;
import io.arex.inst.runtime.log.LogManager;

public class RedissonHelper {

    private static final Method GET_CONFIG = ReflectUtil.getMethod(ConnectionManager.class, "getConfig");
    private static final Set<MasterSlaveServersConfig> MASTER_SLAVE_SERVERS_CONFIGS = new HashSet<>(1);

    private RedissonHelper() {
    }

    /**
     * redisson version < 3.20.0 && >= 3.17.3 ConnectionManager.getConfig().getMasterAddress() >= 3.20.0
     * ConnectionManager.getServiceManager().getConfig().getMasterAddress();
     */
    public static String getRedisUri(ConnectionManager connectionManager) {
        // reflect call (< 3.20.0 && >= 3.17.3)
        if (GET_CONFIG != null) {
            return connectionManager.getConfig().getMasterAddress();
        }
        // reflect call (>= 3.20.0)
        if (!MASTER_SLAVE_SERVERS_CONFIGS.isEmpty()) {
            return MASTER_SLAVE_SERVERS_CONFIGS.iterator().next().getMasterAddress();
        }
        final MasterSlaveServersConfig masterSlaveServersConfig = getMasterSlaveServersConfig(connectionManager);
        if (masterSlaveServersConfig == null) {
            return null;
        }
        MASTER_SLAVE_SERVERS_CONFIGS.add(masterSlaveServersConfig);
        return masterSlaveServersConfig.getMasterAddress();
    }

    public static MasterSlaveServersConfig getMasterSlaveServersConfig(ConnectionManager connectionManager) {
        try {
            // ServerManager
            final Method getServiceMangerMethod = connectionManager.getClass().getMethod("getServiceManager");
            final Object object = getServiceMangerMethod.invoke(connectionManager);
            if (object == null) {
                return null;
            }
            // MasterSlaveServersConfig
            final Method getMasterSlaveServersConfig = object.getClass().getMethod("getConfig");
            return (MasterSlaveServersConfig) getMasterSlaveServersConfig.invoke(object);
        } catch (Exception e) {
            LogManager.warn("redis.masterSlaveConfig", e);
            return null;
        }
    }
}
