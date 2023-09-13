package io.arex.inst.redisson.v3.util;

import org.redisson.config.Config;
import org.redisson.connection.ConnectionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;

public class RedisUtil {
    /**
     *
     */
    public static String getRedisUri(ConnectionManager connectionManager) {
        StringBuilder peer = new StringBuilder();
        try {
            Config config = null;
            try {
                Method getCfg = connectionManager.getClass().getMethod("getCfg");
                getCfg.setAccessible(true);
                config = (Config) getCfg.invoke(connectionManager);
            } catch (NoSuchMethodException e) {
                try {
                    //兼容Redisson3.20.1的版本
                    Method getServiceManager = connectionManager.getClass().getMethod("getServiceManager");
                    getServiceManager.setAccessible(true);
                    Object serviceManager = getServiceManager.invoke(connectionManager);
                    Method method = serviceManager.getClass().getMethod("getCfg");
                    method.setAccessible(true);
                    config = (Config) method.invoke(serviceManager);
                } catch (Exception ex) {
                    System.out.println("getServiceManager.getCfg error " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("getCfg error " + e.getMessage());
            }
            if (config == null) {
                System.out.println("config is null, redisson not support this version");
                return "";
            }
            Object singleServerConfig = ClassUtil.getObjectField(config, "singleServerConfig");
            Object sentinelServersConfig = ClassUtil.getObjectField(config, "sentinelServersConfig");
            Object masterSlaveServersConfig = ClassUtil.getObjectField(config, "masterSlaveServersConfig");
            Object clusterServersConfig = ClassUtil.getObjectField(config, "clusterServersConfig");
            Object replicatedServersConfig = ClassUtil.getObjectField(config, "replicatedServersConfig");
            if (singleServerConfig != null) {
                Object singleAddress = ClassUtil.getObjectField(singleServerConfig, "address");
                peer.append(getPeer(singleAddress));
                return peer.toString();
            }
            if (sentinelServersConfig != null) {
                appendAddresses(peer, (Collection) ClassUtil.getObjectField(sentinelServersConfig, "sentinelAddresses"));
                return peer.toString();
            }
            if (masterSlaveServersConfig != null) {
                Object masterAddress = ClassUtil.getObjectField(masterSlaveServersConfig, "masterAddress");
                peer.append(getPeer(masterAddress));
                appendAddresses(peer, (Collection) ClassUtil.getObjectField(masterSlaveServersConfig, "slaveAddresses"));
                return peer.toString();
            }
            if (clusterServersConfig != null) {
                appendAddresses(peer, (Collection) ClassUtil.getObjectField(clusterServersConfig, "nodeAddresses"));
                return peer.toString();
            }
            if (replicatedServersConfig != null) {
                appendAddresses(peer, (Collection) ClassUtil.getObjectField(replicatedServersConfig, "nodeAddresses"));
                return peer.toString();
            }
        } catch  (Exception e) {
            System.out.println("getRedisUri error " + e.getMessage());
        }
        System.out.println(String.format("getRedisUri: %s", peer));
        return peer.toString();
    }

    private static void appendAddresses(StringBuilder peer, Collection nodeAddresses) {
        if (nodeAddresses != null && !nodeAddresses.isEmpty()) {
            for (Object uri : nodeAddresses) {
                peer.append(getPeer(uri)).append(";");
            }
        }
    }

    static String getPeer(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).replace("redis://", "");
        } else if (obj instanceof URI) {
            URI uri = (URI) obj;
            return uri.getHost() + ":" + uri.getPort();
        } else {
            System.out.println("redisson not support this version");
            return null;
        }
    }
}
