package io.arex.inst.netty.v3.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerCodecTracingHandlerTest {
    @Test
    void init() {
        assertNotNull(new ServerCodecTracingHandler());
    }
}