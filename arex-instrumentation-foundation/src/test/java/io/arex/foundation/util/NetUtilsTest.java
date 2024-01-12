package io.arex.foundation.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * @since 2024/1/12
 */
class NetUtilsTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getIpAddress() {
        try (MockedStatic<NetUtils> netUtils = mockStatic(NetUtils.class)) {
            netUtils.when(NetUtils::getIpAddress).thenReturn("127.0.0.1");
            assertEquals("127.0.0.1", NetUtils.getIpAddress());
        }
    }

    @Test
    void checkTcpPortAvailable() {
        assertEquals(-1, NetUtils.checkTcpPortAvailable(1));

        assertEquals(8080, NetUtils.checkTcpPortAvailable(8080));
    }

    @Test
    void isTcpPortAvailable() {
        assertFalse(NetUtils.isTcpPortAvailable(1));

        assertTrue(NetUtils.isTcpPortAvailable(8080));
    }
}
