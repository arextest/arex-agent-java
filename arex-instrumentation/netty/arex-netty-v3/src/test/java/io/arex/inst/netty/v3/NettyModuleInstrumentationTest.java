package io.arex.inst.netty.v3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NettyModuleInstrumentationTest {
    @Test
    void instrumentationTypes() {
        assertNotNull(new NettyModuleInstrumentation().instrumentationTypes());
    }
}