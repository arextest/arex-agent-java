package io.arex.foundation.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * NetworkInterfaceManager
 */
public enum NetworkInterfaceManager {
    INSTANCE;

    private InetAddress local;

    private InetAddress localHost;

    private NetworkInterfaceManager() {
        load();
    }

    public InetAddress findValidateIp(List<InetAddress> addresses) {
        InetAddress local = null;
        int maxWeight = -1;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                int weight = 0;

                if (address.isSiteLocalAddress()) {
                    weight += 8;
                }

                if (address.isLinkLocalAddress()) {
                    weight += 4;
                }

                if (address.isLoopbackAddress()) {
                    weight += 2;
                }

                // has host name
                if (!Objects.equals(address.getHostName(), address.getHostAddress())) {
                    weight += 1;
                }

                if (weight > maxWeight) {
                    maxWeight = weight;
                    local = address;
                }
            }
        }
        return local;
    }

    public String getLocalHostAddress() {
        return local.getHostAddress();
    }

    public String getLocalHostName() {
        try {
            if (null == localHost) {
                localHost = InetAddress.getLocalHost();
            }
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return local.getHostName();
        }
    }

    private String getProperty(String name) {
        String value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
                return;
            } catch (Exception e) {
                System.err.println(e);
                // ignore
            }
        }

        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            List<InetAddress> addresses = new ArrayList<>();
            InetAddress local = null;

            try {
                for (NetworkInterface ni : nis) {
                    if (ni.isUp() && !ni.isLoopback()) {
                        addresses.addAll(Collections.list(ni.getInetAddresses()));
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            this.local = local;
        } catch (SocketException e) {
            // ignore it
        }
    }
}
