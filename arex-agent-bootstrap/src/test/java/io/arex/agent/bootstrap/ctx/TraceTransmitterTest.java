package io.arex.agent.bootstrap.ctx;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TraceTransmitterTest {

    @Test
    void create() {
        assertNotNull(TraceTransmitter.create());
        Mockito.mockStatic(ArexThreadLocal.Transmitter.class);
        Mockito.when(ArexThreadLocal.Transmitter.capture()).thenReturn("mock");
        TraceTransmitter transmitter = TraceTransmitter.create();
        assertNotNull(transmitter);
        transmitter.transmit();
        transmitter.close();
    }

    @Test
    void doNothingTransmit() {
        TraceTransmitter.DoNothingTransmitter.INSTANCE.transmit();
    }

    @Test
    void doNothingClose() {
        TraceTransmitter.DoNothingTransmitter.INSTANCE.close();
    }
}