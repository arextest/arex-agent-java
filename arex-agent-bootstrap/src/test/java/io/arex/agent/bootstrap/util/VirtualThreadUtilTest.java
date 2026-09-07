package io.arex.agent.bootstrap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadUtilTest {

    @Test
    void isCarrierThread() {
        // current thread of the unit test is a normal platform thread
        assertFalse(VirtualThreadUtil.isCarrierThread());
        assertFalse(VirtualThreadUtil.isCarrierThread((Thread) null));
        assertFalse(VirtualThreadUtil.isCarrierThread(Thread.currentThread()));
        assertFalse(VirtualThreadUtil.isCarrierThread("java.lang.Thread"));
        assertTrue(VirtualThreadUtil.isCarrierThread("jdk.internal.misc.CarrierThread"));
    }
}
