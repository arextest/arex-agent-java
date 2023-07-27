package io.arex.agent.bootstrap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectUtilTest {

    @Test
    void getFieldOrInvokeMethod() throws Exception {
        assertEquals(0, ReflectUtil.getFieldOrInvokeMethod(() -> String.class.getDeclaredField("hash"),"mock"));
        assertNull(ReflectUtil.getFieldOrInvokeMethod(() -> System.class.getDeclaredMethod("checkIO"), null));
    }
}