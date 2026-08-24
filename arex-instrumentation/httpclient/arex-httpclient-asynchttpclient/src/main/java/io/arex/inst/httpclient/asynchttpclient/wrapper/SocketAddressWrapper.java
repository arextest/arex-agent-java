package io.arex.inst.httpclient.asynchttpclient.wrapper;

import java.net.SocketAddress;

public class SocketAddressWrapper extends SocketAddress {
    private String string;

    public SocketAddressWrapper(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
