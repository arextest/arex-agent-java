package io.arex.foundation.util;

import java.net.InetAddress;

/**
 * NetUtils
 *
 *
 * @date 2022/03/16
 */
public class NetUtils {
    public static String getIpAddress() {
        return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
    }
}
