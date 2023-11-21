package io.arex.inst.redisson.v3.common;

import io.arex.agent.bootstrap.util.ReflectUtil;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.connection.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.arex.inst.runtime.log.LogManager;

public class RedissonHelper {

    private static Method getConfigMethod = ReflectUtil.getMethod(ConnectionManager.class, "getConfig");
    private static Set<MasterSlaveServersConfig> masterSlaveServersConfigSet = new HashSet<>(1);

    private RedissonHelper() {
    }

    /**
     * redisson vesion
     * < 3.20.0 && >= 3.17.3 ConnectionManager.getConfig().getMasterAddress()
     * >= 3.20.0 ConnectionManager.getServiceManager().getConfig().getMasterAddress();
     */
    public static String getRedisUri(ConnectionManager connectionManager) {
        // reflect call (< 3.20.0 && >= 3.17.3)
        if (getConfigMethod != null) {
            return connectionManager.getConfig().getMasterAddress();
        }
        // reflect call (>= 3.20.0)
        if (!masterSlaveServersConfigSet.isEmpty()) {
            return masterSlaveServersConfigSet.iterator().next().getMasterAddress();
        }
        final MasterSlaveServersConfig masterSlaveServersConfig = getMasterSlaveServersConfig(connectionManager);
        if (masterSlaveServersConfig == null) {
            return null;
        }
        masterSlaveServersConfigSet.add(masterSlaveServersConfig);
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
