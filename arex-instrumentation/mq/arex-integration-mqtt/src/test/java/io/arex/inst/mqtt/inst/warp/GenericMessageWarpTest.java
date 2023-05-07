package io.arex.inst.mqtt.inst.warp;

import io.arex.inst.mqtt.warp.GenericMessageWarp;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author : MentosL
 * @date : 2023/5/21 16:16
 */
public class GenericMessageWarpTest {


    @Test
    void GenericMessageWarp1(){
        assertNotNull(new GenericMessageWarp(new Object()));
        assertThrows(IllegalArgumentException.class,()-> new GenericMessageWarp(null));
    }

    @Test
    void GenericMessageWarp2(){
        assertNotNull(new GenericMessageWarp(new Object(),new HashMap()));
        assertThrows(IllegalArgumentException.class,()-> new GenericMessageWarp(null,new HashMap()));
    }


    @Test
    void GenericMessageWarp3(){
        assertNotNull(new GenericMessageWarp(new Object(),new MessageHeaders(new HashMap<>())));
        assertThrows(IllegalArgumentException.class,()-> new GenericMessageWarp(null,new MessageHeaders(new HashMap<>())));
        assertThrows(IllegalArgumentException.class,()-> new GenericMessageWarp(new Object(),null));
    }

    @Test
    void removeHeader(){
        GenericMessageWarp genericMessageWarp = new GenericMessageWarp(new Object());
        genericMessageWarp.removeHeader("id");
        assertEquals(1, genericMessageWarp.getMessageHeaderWarp().size());
        assertEquals(2, genericMessageWarp.getHeaders().size());
    }


    @Test
    void put(){
        GenericMessageWarp genericMessageWarp = new GenericMessageWarp(new Object());
        genericMessageWarp.put("mock-key","mock-value");
        assertEquals(3, genericMessageWarp.getMessageHeaderWarp().size());
        assertEquals(2, genericMessageWarp.getHeaders().size());
    }


    @Test
    void get(){
        GenericMessageWarp genericMessageWarp = new GenericMessageWarp(new Object());
        genericMessageWarp.put("mock-key","mock-value");
        assertEquals(3, genericMessageWarp.getMessageHeaderWarp().size());
        assertEquals(2, genericMessageWarp.getHeaders().size());
        assertSame("mock-value",genericMessageWarp.get("mock-key"));
    }

}
