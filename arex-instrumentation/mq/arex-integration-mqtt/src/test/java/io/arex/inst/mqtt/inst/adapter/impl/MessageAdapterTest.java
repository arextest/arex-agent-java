package io.arex.inst.mqtt.inst.adapter.impl;

import io.arex.inst.mqtt.adapter.MessageAdapterImpl;
import io.arex.inst.mqtt.warp.GenericMessageWarp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
/**
 * @author : MentosL
 * @date : 2023/5/11 16:27
 */
public class MessageAdapterTest {

    MessageAdapterImpl instance = MessageAdapterImpl.getInstance();
    MessageChannel messageChannel = Mockito.mock(MessageChannel.class);
    Message message = Mockito.mock(Message.class);
    GenericMessage genericMessage = Mockito.mock(GenericMessage.class);
    GenericMessageWarp genericMessageWarp = Mockito.mock(GenericMessageWarp.class);


    @Test
    void getInstance() {
        assertNotNull(instance);
    }

    @Test
    void getMsg(){
        assertNotNull(instance.getMsg(messageChannel,message));
        assertTrue(instance.getMsg(messageChannel,message).length == 0);
        assertInstanceOf(byte[].class,instance.getMsg(messageChannel,message));
    }

    @Test
    void warpMC(){
        assertNotNull(instance.warpMC(messageChannel));
        assertInstanceOf(MessageChannel.class,instance.warpMC(messageChannel));
        assertNull(instance.warpMC(new Object()));
    }

    @Test
    void warpMessage(){
        assertNotNull(instance.warpMessage(message));
        assertNull(instance.warpMessage(null));
        assertInstanceOf(GenericMessageWarp.class,instance.warpMessage(genericMessageWarp));
        when(genericMessage.getPayload()).thenReturn(new Object());
        assertInstanceOf(GenericMessage.class,instance.warpMessage(genericMessage));
    }

    @Test
    void getHeader(){
        assertNull(instance.getHeader(messageChannel,message,null));
        assertNull(instance.getHeader(messageChannel,message,""));
        Map<String,Object> temp =  new HashMap<>();
        temp.put("mock-key","mock-value");
        when(genericMessage.getHeaders()).thenReturn(new MessageHeaders(temp));
        assertSame("mock-value",instance.getHeader(messageChannel,genericMessage,"mock-key"));
    }

    @Test
    void removeHeader(){
        assertFalse(instance.removeHeader(messageChannel,null,"mock-key"));
        assertFalse(instance.removeHeader(messageChannel,genericMessageWarp,""));
        Map<String,Object> temp =  new HashMap<>();
        temp.put("mock-key","mock-value");
        when(genericMessageWarp.getHeaders()).thenReturn(new MessageHeaders(temp));
        assertTrue(instance.removeHeader(messageChannel,genericMessageWarp,"mock-key"));

        assertFalse(instance.removeHeader(messageChannel,message,"mock-key"));
        assertFalse(instance.removeHeader(messageChannel,genericMessage,"mock-key"));
    }

    @Test
    void addHeader(){
        assertFalse(instance.addHeader(messageChannel,null,"mock-key","mock-value"));
        assertFalse(instance.addHeader(messageChannel,message,"mock-key","mock-value"));
        assertFalse(instance.addHeader(messageChannel,genericMessage,"mock-key","mock-value"));

        when(genericMessageWarp.getHeaders()).thenReturn(new MessageHeaders(new HashMap<>()));
        assertTrue(instance.addHeader(messageChannel,genericMessageWarp,"mock-key","mock-value"));
    }

    @Test
    void resetMsg(){
        assertNull(instance.resetMsg(null));

        Map<String,Object> temp =  new HashMap<>();
        temp.put("mock-key","mock-value");
        when(genericMessageWarp.getHeaders()).thenReturn(new MessageHeaders(temp));

        assertNotNull(instance.resetMsg(genericMessageWarp));
        assertTrue(instance.resetMsg(genericMessageWarp).getHeaders().size()  == 3);
        assertSame("mock-value",instance.resetMsg(genericMessageWarp).getHeaders().get("mock-key"));
    }
}
