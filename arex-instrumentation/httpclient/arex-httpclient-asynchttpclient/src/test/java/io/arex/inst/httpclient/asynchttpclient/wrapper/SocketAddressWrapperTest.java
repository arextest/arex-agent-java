package io.arex.inst.httpclient.asynchttpclient.wrapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SocketAddressWrapperTest {

    @Test
    void testToString() {
        SocketAddressWrapper socketAddressWrapper = new SocketAddressWrapper("test");
        assertEquals("test", socketAddressWrapper.toString());
    }
}