package io.arex.inst.mqtt.inst.inst;


import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.mqtt.MQTTAdapterHelper;
import io.arex.inst.mqtt.inst.MQGenericInstrumentationV3;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.AbstractMessageSendingTemplate;
import org.springframework.messaging.core.GenericMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class MQGenericInstrumentationV3Test {
    MQGenericInstrumentationV3 inst = new MQGenericInstrumentationV3();


    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MQTTAdapterHelper.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }


    @Test
    void typeMatcher() {
        assertFalse(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(AbstractMessageSendingTemplate.class)));
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(GenericMessagingTemplate.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }


    @Test
    void ServiceAdvice_onEnter() {
        Mockito.when(MQTTAdapterHelper.onServiceEnter(any(), any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> MQGenericInstrumentationV3.ArrivedAdvice.onEnter(null, null,null));

        MessageChannel messageChannel = Mockito.mock(MessageChannel.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(MQTTAdapterHelper.onServiceEnter(any(), any(), any())).thenReturn(Pair.of(messageChannel, message));
        assertDoesNotThrow(() -> MQGenericInstrumentationV3.ArrivedAdvice.onEnter(null, null,null));
    }

    @Test
    void ServiceAdvice_onExit() {
        assertDoesNotThrow(() -> MQGenericInstrumentationV3.ArrivedAdvice.onExit(null, null,null));
    }
}
