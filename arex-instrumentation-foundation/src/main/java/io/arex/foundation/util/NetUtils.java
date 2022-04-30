package io.arex.foundation.util;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;

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

    public static int checkTcpPortAvailable(int port) {
        int checkNum = 1;
        while (!isTcpPortAvailable(port)) {
            if (checkNum > 10) {
                return -1;
            }
            checkNum ++;
            port ++;
        }
        return port;
    }

    public static boolean isTcpPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
                    InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
