package io.arex.inst.mqtt.inst.inst;

import io.arex.inst.mqtt.inst.MQTTAdapterModuleInstrumentation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author : MentosL
 * @date : 2023/5/21 15:19
 */
public class MQTTAdapterModuleInstrumentationTest {
    MQTTAdapterModuleInstrumentation module = new MQTTAdapterModuleInstrumentation();


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void instrumentationTypes() {
        assertEquals(1, module.instrumentationTypes().size());
    }
}
