package io.arex.inst.mqtt.inst.warp;

import io.arex.inst.mqtt.warp.MessageHeaderWarp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author : MentosL
 * @date : 2023/5/21 15:39
 */
public class MessageHeaderWarpTest {

    MessageHeaders messageHeaders = Mockito.mock(MessageHeaders.class);

    @Test
    void MessageHeaderWarp1(){
        assertNotNull(new MessageHeaderWarp(messageHeaders));
        MessageHeaders messageHeadersTemp = new MessageHeaders(new HashMap<>());
        assertTrue(new MessageHeaderWarp(messageHeadersTemp).size() == 2);
    }

    @Test
    void MessageHeaderWarp2(){
        assertNotNull(new MessageHeaderWarp(new HashMap<>()));
        assertTrue(new MessageHeaderWarp(new HashMap<>()).size() == 2);
    }

    @Test
    void MessageHeaderWarp3(){
        assertNotNull(new MessageHeaderWarp(new HashMap<>(), UUID.randomUUID(),System.currentTimeMillis()));
        assertTrue(new MessageHeaderWarp(new HashMap<>(), UUID.randomUUID(),System.currentTimeMillis()).size() == 2);
    }

    @Test
    void put(){
        assertNotNull(new MessageHeaderWarp(new HashMap<>()).put("mock-key","mock-value"));
    }

    @Test
    void remove(){
        assertDoesNotThrow(() -> new MessageHeaderWarp(new HashMap<>()).remove("mock-key"));
        MessageHeaderWarp messageHeaderWarp = new MessageHeaderWarp(new HashMap<>(), UUID.randomUUID(), System.currentTimeMillis());
        assertSame(2,messageHeaderWarp.size());
        messageHeaderWarp.remove("id");
        assertSame(1,messageHeaderWarp.size());
    }


}
